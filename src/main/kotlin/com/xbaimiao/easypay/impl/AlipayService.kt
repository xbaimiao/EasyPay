package com.xbaimiao.easypay.impl

import com.alipay.api.DefaultAlipayClient
import com.alipay.api.domain.AlipayTradePrecreateModel
import com.alipay.api.domain.AlipayTradeQueryModel
import com.alipay.api.internal.util.AlipayLogger
import com.alipay.api.request.AlipayTradePrecreateRequest
import com.alipay.api.request.AlipayTradeQueryRequest
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.entity.PayService
import java.util.concurrent.CompletableFuture

/**
 * AlipayService
 *
 * @author xbaimiao
 * @since 2023/9/12 23:10
 */
class AlipayService(
    private val appid: String,
    private val privateKey: String,
    private val publicKey: String,
    private val api: String,
    private val notify: String,
    private val storeId: String
) : PayService {

    private val alipayClient by lazy {
        DefaultAlipayClient(api, appid, privateKey, "json", "UTF-8", publicKey, "RSA2").also {
            AlipayLogger.setNeedEnableLogger(false)
        }
    }

    override val name: String = "alipay"

    override fun createOrder(item: Item): Order {
        val request = AlipayTradePrecreateRequest()
        request.notifyUrl = notify
        val tradeNo = generateOrderId()

        val model = AlipayTradePrecreateModel()
        model.outTradeNo = tradeNo
        model.totalAmount = String.format("%.2f", item.price)
        model.subject = item.name
        model.storeId = storeId
        model.qrCodeTimeoutExpress = "5m"

        request.bizModel = model

        val response = alipayClient.execute(request)
        if (response.isSuccess) {
            debug("create ${item.name} ${response.body}")
            return Order(tradeNo, item, response.qrCode)
        }
        error("create order fail!")
    }

    override fun createOrderCall(
        item: Item,
        call: Order.() -> Unit,
        timeout: Order.() -> Unit
    ): CompletableFuture<Order> {
        val future = CompletableFuture<Order>()
        schedule {
            val order = async {
                createOrder(item)
            }
            future.complete(order)
            // 查询5分钟 查询一次等待1秒
            for (index in 0..(60 * 5)) {
                val status = async {
                    queryOrder(order)
                }
                // 如果已经支付跳出循环调用回调方法
                if (status == OrderStatus.SUCCESS) {
                    call.invoke(order)
                    return@schedule
                }
                // 等待1秒在查询
                waitFor(20)
            }
            timeout.invoke(order)
        }
        return future
    }

    override fun queryOrder(order: Order): OrderStatus {
        val query = AlipayTradeQueryRequest()

        val model = AlipayTradeQueryModel()
        model.outTradeNo = order.orderId
        model.tradeNo = ""
        query.bizModel = model

        val result = alipayClient.execute(query)
        debug("query $order ${result.body}")
        return when (result.tradeStatus) {
            // 支付成功
            "TRADE_SUCCESS" -> OrderStatus.SUCCESS
            // 等待支付
            "WAIT_BUYER_PAY" -> OrderStatus.WAIT_PAY
            null -> OrderStatus.WAIT_SCAN
            else -> OrderStatus.UNKNOWN
        }
    }

}