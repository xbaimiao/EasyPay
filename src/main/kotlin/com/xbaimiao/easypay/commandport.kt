package com.xbaimiao.easypay

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.command.debugCommand
import com.xbaimiao.easylib.nms.sendMap
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.entity.PayServiceProvider
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
                                    Database.inst().addOrder(player.name, it)
                                }
                                player.updateInventory()
                                it.item.sendTo(player, service)
                            },
                            timeout = {
                                player.updateInventory()
                            }
                        ) {
                            player.sendLang("command-item-cancel")
                            player.updateInventory()
                        }.thenAccept {
                            if (it.isPresent) {
                                val order = it.get()
                                order.item.onCreate(player, service)
                                player.sendLang("command-create-success", order.item.price.toString())
                                player.sendMap(ZxingUtil.generate(order.qrCode))
                            } else {
                                error("failed to get present order")
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
    onlinePlayers { playerArg ->
        exec {
            schedule(SynchronizationContext.ASYNC) {
                for (player in valueOf(playerArg)) {
                    sender.sendMessage(player.name)
                    for (order in Database.inst().getAllOrder(player.name)) {
                        sender.sendMessage(order.toString())
                    }
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