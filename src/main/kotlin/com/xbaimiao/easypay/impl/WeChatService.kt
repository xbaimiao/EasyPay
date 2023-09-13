package com.xbaimiao.easypay.impl

import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import dev.rgbmc.walletconnector.WalletConnector
import java.util.*

/**
 * WeChatService
 *
 * @author FlyProject
 * @since 2023/9/13 14:38
 */
class WeChatService(
    private val server: String,
    private val qrcodeContent: String
) : DefaultPayService {

    private val walletConnector: WalletConnector = WalletConnector()

    init {
        walletConnector.connect(server)
    }

    override fun timeOut(timeout: Order.() -> Unit, order: Order) {
        walletConnector.orderTimeout(order.item.price)
        timeout.invoke(order)
    }

    override val name: String = "wechat"

    override fun createOrder(item: Item): Optional<Order> {
        if (list.contains(item.price)) {
            // Cancel Order
            return Optional.empty()
        }
        val tradeNo = generateOrderId()
        // Join?
        val status = walletConnector.createOrder(item.price).join()
        if (!status) {
            // Cancel Order [multi-servers]
            return Optional.empty()
        }
        list.add(item.price)
        walletConnector.listenOrder(item.price) {
            list.remove(item.price)
        }
        return Optional.of(Order(tradeNo, item, qrcodeContent))
    }

    override fun queryOrder(order: Order): OrderStatus {
        debug("queryWechat $order Price: ${order.item.price}")
        return when (list.contains(order.item.price)) {
            // 支付成功
            false -> OrderStatus.SUCCESS
            else -> OrderStatus.WAIT_SCAN
        }
    }

    companion object {
        private val list: MutableList<Double> = ArrayList()
    }
}