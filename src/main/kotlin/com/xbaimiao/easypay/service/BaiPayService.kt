package com.xbaimiao.easypay.service

import com.xbaimiao.baipay.sdk.BaiPayClient
import com.xbaimiao.baipay.sdk.BaiPayException
import com.xbaimiao.baipay.sdk.model.CreateOrderRequest
import com.xbaimiao.baipay.sdk.model.PaymentChannel
import com.xbaimiao.baipay.sdk.model.OrderStatus as BaiPayOrderStatus
import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import java.util.concurrent.CompletableFuture

class BaiPayService(
    private val client: BaiPayClient,
    private val channel: PaymentChannel,
    private val returnUrl: String?,
    private val waitTime: Int
) : DefaultPayService {

    override val name: String = when (channel) {
        PaymentChannel.WECHAT -> "baipay-wechat"
        PaymentChannel.ALIPAY -> "baipay-alipay"
    }

    override val displayName: String = when (channel) {
        PaymentChannel.WECHAT -> "BaiPay微信"
        PaymentChannel.ALIPAY -> "BaiPay支付宝"
    }

    override val logoFile: File
        get() = when (channel) {
            PaymentChannel.WECHAT -> ZxingUtil.wechatLogo
            PaymentChannel.ALIPAY -> ZxingUtil.alipayLogo
        }

    override fun createOrderCall(
        player: String,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Order?> {
        return createOrderCall(
            player,
            item,
            call,
            { order ->
                closeRemoteAsync(order)
                timeout.invoke(this, order)
            },
            cancel,
            waitTime
        )
    }

    override fun createOrder(player: String, item: Item): Order? {
        return try {
            val amountCents = BigDecimal.valueOf(item.price)
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
            val created = client.createOrder(
                CreateOrderRequest(
                    amountCents,
                    item.name,
                    null,
                    player,
                    item.name,
                    returnUrl,
                    channel,
                    UUID.randomUUID().toString()
                )
            )
            val orderId = created.orderId?.takeIf(String::isNotBlank)
            val payAmountCents = created.payAmountCents
            val qrCode = created.qrCode?.takeIf(String::isNotBlank)
            if (orderId == null || payAmountCents == null || qrCode == null) {
                warn("BaiPay创建订单返回的数据不完整")
                null
            } else {
                Order(
                    orderId,
                    item,
                    qrCode,
                    this,
                    BigDecimal.valueOf(payAmountCents).movePointLeft(2).toDouble()
                )
            }
        } catch (exception: BaiPayException) {
            warn("BaiPay创建订单失败 [${exception.code}] ${exception.message}")
            null
        } catch (exception: Exception) {
            warn("BaiPay创建订单失败: ${exception.message}")
            null
        }
    }

    override fun queryOrder(order: Order): OrderStatus {
        return try {
            when (client.getOrder(order.orderId).orderStatus) {
                BaiPayOrderStatus.PAID -> OrderStatus.SUCCESS
                BaiPayOrderStatus.PENDING -> OrderStatus.WAIT_PAY
                BaiPayOrderStatus.CLOSED,
                BaiPayOrderStatus.EXPIRED,
                BaiPayOrderStatus.REFUNDED,
                null -> OrderStatus.UNKNOWN
            }
        } catch (exception: Exception) {
            debug("BaiPay查询订单失败 ${order.orderId}: ${exception.message}")
            OrderStatus.UNKNOWN
        }
    }

    override fun close(order: Order) {
        super<DefaultPayService>.close(order)
        closeRemoteAsync(order)
    }

    private fun closeRemoteAsync(order: Order) {
        launchCoroutine(SynchronizationContext.ASYNC) {
            try {
                client.closeOrder(order.orderId)
            } catch (exception: Exception) {
                debug("BaiPay关闭订单失败 ${order.orderId}: ${exception.message}")
            }
        }
    }
}
