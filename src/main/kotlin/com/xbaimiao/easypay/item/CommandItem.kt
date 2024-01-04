package com.xbaimiao.easypay.item

import com.xbaimiao.easylib.bridge.player.parseECommand
import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * CommandItem
 *
 * @author xbaimiao
 * @since 2023/9/13 10:43
 */
data class CommandItem(
    override var price: Double,
    override val name: String,
    private val command: List<String>,
) : AbstractItem() {

    override fun sendTo(player: Player, service: PayService?, order: Order): Collection<String> {
        val cmdList = command.toMutableList()
        cmdList.replaceAll { it.replace("%item_name%", name) }
        cmdList.parseECommand(player).exec(Bukkit.getConsoleSender())

        return cmdList.map { it.replace("%player_name%", player.name).replacePlaceholder(player) }
//        FunctionUtil.parseActions(player, order, service, rewards)
    }

}