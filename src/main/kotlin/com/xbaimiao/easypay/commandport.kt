package com.xbaimiao.easypay

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.nms.sendMap
import com.xbaimiao.easypay.api.ItemProvider
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
                        service.createOrderCall(
                            item = item,
                            call = {
                                player.updateInventory()
                                this.item.sendTo(player)
                            },
                            timeout = {
                                player.updateInventory()
                            }
                        ).thenAccept {
                            player.sendMap(ZxingUtil.generate(it.qrCode))
                        }
                    }
                }
            }
        }
    }
}

val rootCommand = command<CommandSender>("easypay") {
    description = "主要命令"
    permission = "easypay.command"
    sub(create)
}