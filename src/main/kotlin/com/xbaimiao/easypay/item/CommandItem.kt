package com.xbaimiao.easypay.item

import com.xbaimiao.easylib.util.eu.parseECommand
import com.xbaimiao.easypay.FunctionUtil
import com.xbaimiao.easypay.api.Item
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
    private val actions: List<String>,
    private val preActions: List<String>,
    private val rewards: List<String>
) : Item {

    override fun preCreate(player: Player, service: PayService, order: Order): Boolean {
        return FunctionUtil.parseActions(player, order, service, preActions)
    }

    override fun onCreate(player: Player, service: PayService, order: Order) {
        FunctionUtil.parseActions(player, order, service, actions)
    }

    override fun sendTo(player: Player, service: PayService, order: Order) {
        val cmdList = command.toMutableList()
        cmdList.replaceAll { it.replace("%item_name%", name) }
        cmdList.parseECommand(player).exec(Bukkit.getConsoleSender())

        FunctionUtil.parseActions(player, order, service, rewards)
    }

    companion object {
        private const val serialVersionUID: Long = -8192333653275672125L
    }

}