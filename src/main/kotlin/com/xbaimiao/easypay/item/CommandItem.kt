package com.xbaimiao.easypay.item

import com.xbaimiao.easylib.bridge.player.parseECommand
import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.scripting.GroovyScriptManager
import com.xbaimiao.easypay.util.FunctionUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.logging.Level

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

    override fun sendTo(player: Player, service: PayService, order: Order): Collection<String> {
        val cmdList = command.toMutableList()
        cmdList.replaceAll { it.replace("%item_name%", name) }
        cmdList.parseECommand(player).exec(Bukkit.getConsoleSender())

        GroovyScriptManager.instance.orderReward(player, service, order)
        kotlin.runCatching {
            FunctionUtil.instance.parseActions(player, order, service, rewards)
        }.onFailure {
            plugin.logger.log(Level.SEVERE, it.message, it)
        }
        return cmdList.map {
            it.replace("%player_name%", player.name).replace("%item_name%", name).replacePlaceholder(player)
        }
    }

}
