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
import dev.rgbmc.alipayconnector.AliPayConnector
import org.bukkit.Bukkit
import java.io.File
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

/**
 * DLCAliPayService
 *
 * @author FlyProject
 * @since 2024/8/1 16:47
 */
class DLCAliPayService(
    server: String,
    private val qrcodeContent: String
) : DefaultPayService {

    val aliPayConnector: AliPayConnector = AliPayConnector()
    private var counter: Int = 0
    private var connected: Boolean = false

    init {
        AliPayConnector.setDisconnectCallback {
            connected = false
            counter++
            if (counter >= plugin.config.getInt("alipay-dlc.max-retry")) {
                AliPayConnector.setReconnect(false)
                warn("EasyPay支付宝DLC 已超过最大重连次数 请重载EasyPay后再次尝试链接")
            }
        }
        AliPayConnector.setConnectedCallback {
            info("已连接上EasyPay支付宝DLC")
            counter = 0
            connected = true
        }
        AliPayConnector.setReconnect(true)
        aliPayConnector.connect(server)
    }

    override fun timeOut(timeout: suspend SchedulerController.(Order) -> Unit, order: Order) {
        launchCoroutine {
            orderMap[order.price] = OrderStatus.UNKNOWN
            async {
                aliPayConnector.orderTimeout(order.price)
            }
            timeout.invoke(this, order)
        }
    }

    override val name: String = "alipay-dlc"

    override val displayName: String = "支付宝监听"

    override val logoFile: File
        get() = ZxingUtil.alipayLogo

    override fun createOrderCall(
        player: String,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Order?> {
        return super.createOrderCall(player, item, call, timeout, cancel, plugin.config.getInt("alipay-dlc.wait-time"))
    }

    override fun close(order: Order) {
        super.close(order)
        orderMap[order.price] = OrderStatus.UNKNOWN
        aliPayConnector.orderTimeout(order.item.price)
    }

    override fun createOrder(player: String, item: Item): Order? {
        if (!connected) return null // Waiting Connected
        var newPrice = BigDecimal(item.price.toString())
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        val oldPrice = item.price
        if (orderMap.containsKey(oldPrice) && orderMap[oldPrice] != OrderStatus.UNKNOWN) {
            if (plugin.config.getBoolean("alipay-dlc.dynamic-cost")) {
                // Dynamic Cost
                if (offlinePlayer.isOnline) {
                    offlinePlayer.player?.sendLang("command-wechat-dynamic-cost")
                }
                while (orderMap.contains(newPrice.toDouble())) {
                    newPrice = newPrice.add(BigDecimal("0.01"))
                }
            } else {
                // Cancel Order
                return null
            }
        }
        val tradeNo = generateOrderId()
        val floatNewPrice = newPrice.toDouble()
        val order = Order(tradeNo, item, qrcodeContent, this, floatNewPrice)

        if (offlinePlayer.isOnline) {
            debug("offline player online execute preCreate")
            if (!item.preCreate(offlinePlayer.player!!, this, order)) {
                return null
            }
        }

        // Join?
        // 监听已浮动的价格
        val status = aliPayConnector.createOrder(floatNewPrice, tradeNo).join()
        if (!status) {
            // Cancel Order [multi-servers]
            return null
        }
        orderMap[floatNewPrice] = OrderStatus.WAIT_SCAN
        aliPayConnector.listenOrder(floatNewPrice) {
            orderMap.remove(floatNewPrice)
        }
        return order
    }

    override fun queryOrder(order: Order): OrderStatus {
        debug("queryAliPay $order Price: ${order.price}")
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
