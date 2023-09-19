package com.xbaimiao.easypay

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.command.debugCommand
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.OrderData
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.map.MapUtilProvider
import org.bukkit.command.CommandSender

/**
 * commandport
 *
 * @author xbaimiao
 * @since 2023/9/13 10:27
 */
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
                        player.sendLang("command-create-start")
                        service.createOrderCall(
                            player = player,
                            item = item,
                            call = {
                                async {
                                    Database.inst().addOrder(player.name, OrderData.fromOrder(it, player.name))
                                }
                                player.updateInventory()
                                it.item.sendTo(player, service, it)
                            },
                            timeout = {
                                player.sendLang("command-order-timeout")
                                player.updateInventory()
                            }
                        ) {
                            player.sendLang("command-item-cancel")
                            player.updateInventory()
                        }.thenAccept { order ->
                            if (order != null) {
                                schedule {
                                    val qr = async {
                                        ZxingUtil.generate(order.qrCode)
                                    }
                                    order.item.onCreate(player, service, order)
                                    player.sendLang("command-create-success", order.price.toString())
                                    MapUtilProvider.getMapUtil().sendMap(player, qr) {
                                        // onDrop Map
                                        if (MapUtilProvider.getMapUtil().cancelOnDrop) {
                                            order.close()
                                            player.sendLang("command-close-order")
                                        }
                                        player.updateInventory()
                                    }
                                }
                            } else {
                                player.sendLang("command-create-fail")
                            }
                        }
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
            schedule(SynchronizationContext.ASYNC) {
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
        p.loadMap()
        p.loadDatabase()
        p.loadServices()
        p.loadItems()
        sender.sendLang("command-reload")
    }
}

val rootCommand = command<CommandSender>("easypay") {
    description = "主要命令"
    permission = "easypay.command"
    sub(create)
    sub(printAllOrder)
    sub(reload)
    sub(debugCommand)
}