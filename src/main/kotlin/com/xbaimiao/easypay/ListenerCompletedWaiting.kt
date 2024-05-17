package com.xbaimiao.easypay

import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.WebOrder
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayServiceProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@EListener
object ListenerCompletedWaiting : Listener {

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        check(player)
    }

    fun check(player: Player) {
        if (!Database.isInit()) {
            return
        }
        launchCoroutine(SynchronizationContext.ASYNC) {
            if (!player.isOnline) {
                return@launchCoroutine
            }
            val allWebOrder = Database.inst().getWebOrderByPlayer(player.name)
            for (order in allWebOrder) {
                if (order.status == WebOrder.Status.WAIT_DELIVERY) {
                    // 如果是等待发货状态
                    val service = PayServiceProvider.getService(order.payType)
                    if (service == null) {
                        warn("支付服务 ${order.payType} 不在配置文件存在 玩家延迟进服发货失败")
                        continue
                    }
                    val item = if (ItemProvider.isCustomItem(order.desc)) {
                        ItemProvider.getCustomItem(order.price)
                    } else {
                        ItemProvider.getItem(order.desc)
                    }
                    if (item == null) {
                        warn("商品 ${order.desc} 不在配置文件存在 玩家延迟进服发货失败")
                        continue
                    }
                    sendReward(
                        player,
                        Order(order.orderId, item, "TRANSACTION_COMPLETED_WAITING_FOR_DELIVERY", service, order.price),
                        service
                    )
                }
            }
        }
    }

}
