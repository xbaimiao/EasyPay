package com.xbaimiao.easypay

import com.xbaimiao.easylib.bridge.economy.EconomyManager
import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.command.debugCommand
import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.OrderData
import com.xbaimiao.easypay.database.WebOrder
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.item.CustomConfiguration
import com.xbaimiao.easypay.map.MapUtilProvider
import com.xbaimiao.easypay.reward.RewardHandle
import com.xbaimiao.easypay.util.ZxingUtil
import com.xbaimiao.easypay.util.formatTime
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * commandport
 *
 * @author xbaimiao
 * @since 2023/9/13 10:27
 */

suspend fun SchedulerController.sendReward(player: Player, order: Order, service: PayService?) {
    switchContext(SynchronizationContext.SYNC)
    if (!player.isOnline) {
        warn("玩家 ${player.name} 不在线 发货失败 等待重新进服在发货")
        return
    }
    MapUtilProvider.getMapUtil().clearAllMap(player)
    val oldVault = EconomyManager.vault[player]
    val oldPoints = EconomyManager.playerPoints[player]

    val commands = order.item.sendTo(player, service, order)
    val newVault = EconomyManager.vault[player]
    val newPoints = EconomyManager.playerPoints[player]
    async {
        Database.inst().addOrder(player.name, OrderData.fromOrder(order, player.name))
        val webOrder = Database.inst().getWebOrder(order.orderId)
        if (webOrder != null) {
            webOrder.status = WebOrder.Status.SUCCESS
            webOrder.sendTime = System.currentTimeMillis()
            webOrder.sendLog = Lang.asLangText<List<String>>(
                "web-order-send-log",
                System.currentTimeMillis().formatTime(),
                player.name,
                oldVault,
                oldPoints,
                commands.joinToString("  ,  "),
                newVault,
                newPoints
            ).joinToString("\r\n")
            Database.inst().updateWebOrder(webOrder)
        }
    }
}

fun Player.hasEmptySlot(): Boolean {
    return (0..35).any { inventory.getItem(it) == null }
}

private fun handle(player: Player, item: Item, service: PayService) {
    if (!player.hasEmptySlot()) {
        player.sendLang("command-not-empty-slot")
        return
    }
    player.sendLang("command-create-start")
    service.createOrderCall(
        player = player,
        item = item,
        call = {
            if (player.isOnline) {
                // 玩家在线 发货并把status改成 SUCCESS
                sendReward(player, it, service)
            } else {
                warn("玩家 ${player.name} 不在线 将在它下一次进服的时候发货")
            }
        },
        timeout = {
            player.sendLang("command-order-timeout")
            MapUtilProvider.getMapUtil().clearAllMap(player)
        }
    ) {
        player.sendLang("command-item-cancel")
        player.updateInventory()
    }.thenAccept { order ->
        if (order != null) {
            launchCoroutine {
                val qr = async {
                    ZxingUtil.generate(order.qrCode, order.service.logoFile)
                }
                order.item.onCreate(player, service, order)
                player.sendLang("command-create-success", order.price.toString())
                MapUtilProvider.getMapUtil().sendMap(player, qr) {
                    if (MapUtilProvider.getMapUtil().cancelOnDrop) {
                        player.sendLang("command-close-order")
                        player.updateInventory()
                        order.close()
                    }
                }
            }
        } else {
            player.sendLang("command-create-fail")
        }
    }
}

private val payServiceArgNode = ArgNode("支付服务", exec = { token ->
    PayServiceProvider.getAllService().map { it.name }.filter { it.lowercase().startsWith(token.lowercase()) }
}, parse = {
    PayServiceProvider.getService(it)
})

private val itemArgNode = ArgNode("商品", exec = { token ->
    ItemProvider.getAllItem().map { it.name }.filter { it.lowercase().startsWith(token.lowercase()) }
}, parse = {
    ItemProvider.getItem(it)
})

private val create = command<CommandSender>("create") {
    permission = "easypay.command.create"
    description = "创建一个订单"
    onlinePlayers { playerArg ->
        arg(payServiceArgNode) { serviceArg ->
            arg(itemArgNode) { itemArg ->
                exec {
                    val players = valueOf(playerArg)
                    val service = valueOf(serviceArg)
                    if (service == null) {
                        sender.sendLang("command-service-null", valueToString(serviceArg))
                        return@exec
                    }
                    val item = valueOf(itemArg)
                    if (item == null) {
                        sender.sendLang("command-item-null", valueToString(itemArg))
                        return@exec
                    }
                    players.forEach { player ->
                        handle(player, item, service)
                    }
                }
            }
        }
    }
}

private val custom = command<CommandSender>("custom") {
    permission = "easypay.command.custom"
    description = "自定义金额充值"
    onlinePlayers { playerArg ->
        arg(payServiceArgNode) { serviceArg ->
            number { numberArg ->
                exec {
                    val players = valueOf(playerArg)
                    val service = valueOf(serviceArg)
                    if (service == null) {
                        sender.sendLang("command-service-null", valueToString(serviceArg))
                        return@exec
                    }
                    val price = valueOf(numberArg)
                    for (player in players) {
                        handle(
                            player,
                            CustomConfiguration.getCustomPriceItemConfig().createItem(price),
                            service
                        )
                    }
                }
            }
        }
    }
}

private val printAllOrder = command<CommandSender>("print") {
    permission = "easypay.command.print"
    description = "打印指定玩家所有订单"
    offlinePlayers { playerArg ->
        exec {
            launchCoroutine(SynchronizationContext.ASYNC) {
                val player = valueOf(playerArg)
                sender.sendMessage(player)
                for (order in Database.inst().getAllOrder(player)) {
                    sender.sendMessage(order.toString())
                }
            }
        }
    }
}

private val reload = command<CommandSender>("reload") {
    permission = "easypay.command.reload"
    description = "重载插件"
    exec {
        val p = plugin as EasyPay
        p.reloadConfig()
        p.loadCustomConfig()
        p.loadMap()
        p.loadDatabase()
        p.loadServices()
        p.loadItems()
        RewardHandle.loadConfiguration()
        sender.sendLang("command-reload")
    }
}

private val rewardOpen = command<CommandSender>("open") {
    description = "打开累充界面"
    players {
        exec {
            valueOf(it)?.let { it1 -> RewardHandle.open(it1) }
        }
    }
}

private val rewardAdd = command<CommandSender>("add") {
    description = "添加累充金额,减少请用负数,无法直接设置玩家累充金额"
    number {
        offlinePlayers { p ->
            exec {
                val player = valueOf(p)
                val num = valueOf(it)
                Database.inst().addRewardPrice(player, num)
                sender.sendLang("reward-add-success", player, num)
            }
        }
    }
}

private val reward = command<CommandSender>("reward") {
    permission = "easypay.command.reward"
    description = "累充相关命令"
    sub(rewardOpen)
    sub(rewardAdd)
}

private val check = command<CommandSender>("check") {
    description = "检测指定玩家是否还有未发货订单 并发货"
    val playerArg = players()
    exec {
        playerArg.value()?.let { ListenerCompletedWaiting.check(it) }
    }
}

val rootCommand = command<CommandSender>("easypay") {
    description = "主要命令"
    permission = "easypay.command"
    sub(create)
    sub(custom)
    sub(printAllOrder)
    sub(reload)
    sub(reward)
    sub(check)
    sub(debugCommand)
}