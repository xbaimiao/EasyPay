package com.xbaimiao.easypay.service

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import dev.rgbmc.walletconnector.WalletConnector
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * WeChatService
 *
 * @author FlyProject
 * @since 2023/9/13 14:38
 */
class WeChatService(
    server: String,
    private val qrcodeContent: String
) : DefaultPayService {

    val walletConnector: WalletConnector = WalletConnector()

    init {
        WalletConnector.setReconnect(true)
        walletConnector.connect(server)
    }

    override fun timeOut(timeout: suspend SchedulerController.(Order) -> Unit, order: Order) {
        schedule {
            list.remove(order.price)
            async {
                walletConnector.orderTimeout(order.price)
            }
            timeout.invoke(this, order)
        }
    }

    override val name: String = "wechat"

    override fun createOrderCall(
        player: Player,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Order?> {
        return super.createOrderCall(player, item, call, timeout, cancel, plugin.config.getInt("wechat.wait-time"))
    }

    override fun close(order: Order) {
        super.close(order)
        list.remove(order.price)
        walletConnector.orderTimeout(order.item.price)
    }

    override fun createOrder(player: Player, item: Item): Order? {
        var newPrice = item.price
        if (list.contains(newPrice)) {
            if (plugin.config.getBoolean("wechat.dynamic-cost")) {
                // Dynamic Cost
                player.sendLang("command-wechat-dynamic-cost")
                while (list.contains(newPrice)) {
                    newPrice += 0.01
                }
            } else {
                // Cancel Order
                return null
            }
        }
        val tradeNo = generateOrderId()
        val order = Order(tradeNo, item, qrcodeContent, this, newPrice)
        if (!item.preCreate(player, this, order)) {
            return null
        }
        // Join?
        // 监听已浮动的价格
        val status = walletConnector.createOrder(newPrice).join()
        if (!status) {
            // Cancel Order [multi-servers]
            return null
        }
        list.add(newPrice)
        walletConnector.listenOrder(newPrice) {
            list.remove(newPrice)
        }
        return order
    }

    override fun queryOrder(order: Order): OrderStatus {
        debug("queryWechat $order Price: ${order.price}")
        return when (list.contains(order.price)) {
            // 支付成功
            false -> OrderStatus.SUCCESS
            else -> OrderStatus.WAIT_SCAN
        }
    }

    companion object {
        val list: MutableList<Double> = ArrayList()
    }
}