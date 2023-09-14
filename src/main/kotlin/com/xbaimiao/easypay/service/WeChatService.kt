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
import java.util.*
import java.util.concurrent.CompletableFuture

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

    override fun timeOut(timeout: suspend SchedulerController.(Order) -> Unit, order: Order) {
        schedule {
            async {
                walletConnector.orderTimeout(order.item.price)
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
    ): CompletableFuture<Optional<Order>> {
        return super.createOrderCall(player, item, call, timeout, cancel, plugin.config.getInt("wechat.wait-time"))
    }

    override fun createOrder(player: Player, item: Item): Optional<Order> {
        if (list.contains(item.price)) {
            if (plugin.config.getBoolean("wechat.dynamic-cost")) {
                // Dynamic Cost
                player.sendLang("command-wechat-dynamic-cost")
                var newCost = item.price + 0.01;
                while (!list.contains(newCost)) {
                    newCost += 0.01
                }
                item.price = newCost
            } else {
                // Cancel Order
                return Optional.empty()
            }
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