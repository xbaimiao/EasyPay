package com.xbaimiao.easypay.impl

import com.alipay.api.DefaultAlipayClient
import com.alipay.api.domain.AlipayTradePrecreateModel
import com.alipay.api.domain.AlipayTradeQueryModel
import com.alipay.api.internal.util.AlipayLogger
import com.alipay.api.request.AlipayTradePrecreateRequest
import com.alipay.api.request.AlipayTradeQueryRequest
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import java.util.*

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
) : DefaultPayService {

    private val alipayClient by lazy {
        DefaultAlipayClient(api, appid, privateKey, "json", "UTF-8", publicKey, "RSA2").also {
            AlipayLogger.setNeedEnableLogger(false)
        }
    }

    override fun timeOut(timeout: Order.() -> Unit, order: Order) {
        timeout.invoke(order)
    }

    override val name: String = "alipay"

    override fun createOrder(item: Item): Optional<Order> {
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
            return Optional.of(Order(tradeNo, item, response.qrCode))
        }
        error("create order fail!")
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