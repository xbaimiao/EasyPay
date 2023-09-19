package com.xbaimiao.easypay.item

import com.xbaimiao.easylib.util.eu.parseECommand
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.util.FunctionUtil
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
    private val actions: List<String>,
    private val preActions: List<String>,
    private val rewards: List<String>
) : AbstractItem(actions, preActions) {

    override fun sendTo(player: Player, service: PayService, order: Order) {
        val cmdList = command.toMutableList()
        cmdList.replaceAll { it.replace("%item_name%", name) }
        cmdList.parseECommand(player).exec(Bukkit.getConsoleSender())

        FunctionUtil.parseActions(player, order, service, rewards)
    }

}