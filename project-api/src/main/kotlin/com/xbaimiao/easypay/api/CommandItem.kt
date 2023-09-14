package com.xbaimiao.easypay.api

import com.xbaimiao.easylib.util.eu.parseECommand
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
    val actions: List<String>
) : Item {

    override fun sendTo(player: Player) {
        val cmdList = command.toMutableList()
        cmdList.replaceAll { it.replace("%item_name%", name) }
        cmdList.parseECommand(player).exec(Bukkit.getConsoleSender())
    }

    companion object {
        private const val serialVersionUID: Long = -8192333653275672125L
    }

}