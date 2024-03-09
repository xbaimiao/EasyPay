package com.xbaimiao.easypay.service

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import dev.rgbmc.walletconnector.WalletConnector
import org.bukkit.Bukkit
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * WeChatService
 *
 * @author FlyProject
 * @since 2023/9/13 14:38
 */
class DLCWeChatService(
    server: String,
    private val qrcodeContent: String
) : DefaultPayService {

    val walletConnector: WalletConnector = WalletConnector()
    private var counter: Int = 0
    private var connected: Boolean = false

    init {
        WalletConnector.setDisconnectCallback {
            connected = false
            counter++
            if (counter >= plugin.config.getInt("wechat.max-retry")) {
                WalletConnector.setReconnect(false)
                warn("EasyPay微信DLC 已超过最大重连次数 请重载EasyPay后再次尝试链接")
            }
        }
        WalletConnector.setConnectedCallback {
            info("已连接上EasyPay微信DLC")
            counter = 0
            connected = true
        }
        WalletConnector.setReconnect(true)
        walletConnector.connect(server)
    }

    override fun timeOut(timeout: suspend SchedulerController.(Order) -> Unit, order: Order) {
        launchCoroutine {
            orderMap[order.price] = OrderStatus.UNKNOWN
            async {
                walletConnector.orderTimeout(order.price)
            }
            timeout.invoke(this, order)
        }
    }

    override val name: String = "wechat"

    override val displayName: String = "微信监听"

    override val logoFile: File
        get() = ZxingUtil.wechatLogo

    override fun createOrderCall(
        player: String,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Order?> {
        return super.createOrderCall(player, item, call, timeout, cancel, plugin.config.getInt("wechat.wait-time"))
    }

    override fun close(order: Order) {
        super.close(order)
        orderMap[order.price] = OrderStatus.UNKNOWN
        walletConnector.orderTimeout(order.item.price)
    }

    override fun createOrder(player: String, item: Item): Order? {
        if (!connected) return null // Waiting Connected
        var newPrice = item.price
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        if (orderMap.containsKey(newPrice) && orderMap[newPrice] != OrderStatus.UNKNOWN) {
            if (plugin.config.getBoolean("wechat.dynamic-cost")) {
                // Dynamic Cost
                if (offlinePlayer.isOnline) {
                    offlinePlayer.player.sendLang("command-wechat-dynamic-cost")
                }
                while (orderMap.contains(newPrice)) {
                    newPrice += 0.01
                }
            } else {
                // Cancel Order
                return null
            }
        }
        val tradeNo = generateOrderId()
        val order = Order(tradeNo, item, qrcodeContent, this, newPrice)

        if (offlinePlayer.isOnline) {
            debug("offline player online execute preCreate")
            if (!item.preCreate(offlinePlayer.player, this, order)) {
                return null
            }
        }

        // Join?
        // 监听已浮动的价格
        val status = walletConnector.createOrder(newPrice).join()
        if (!status) {
            // Cancel Order [multi-servers]
            return null
        }
        orderMap[newPrice] = OrderStatus.WAIT_SCAN
        walletConnector.listenOrder(newPrice) {
            orderMap.remove(newPrice)
        }
        return order
    }

    override fun queryOrder(order: Order): OrderStatus {
        debug("queryWechat $order Price: ${order.price}")
        return when (orderMap.containsKey(order.price)) {
            // 支付成功
            false -> OrderStatus.SUCCESS
            else -> return orderMap[order.price]!!
        }
    }

    override fun isInteractive(): Boolean {
        return false
    }

    companion object {
        val orderMap: MutableMap<Double, OrderStatus> = mutableMapOf()
    }
}