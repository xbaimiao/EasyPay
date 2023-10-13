package com.xbaimiao.easypay.reward

import com.xbaimiao.easylib.chat.BuiltInConfiguration
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.ui.Variable
import com.xbaimiao.easylib.ui.convertItem
import com.xbaimiao.easylib.util.warn
import org.bukkit.entity.Player

object RewardHandle {

    private lateinit var configuration: BuiltInConfiguration

    fun loadConfiguration() {
        configuration = BuiltInConfiguration("reward.yml")
    }

    fun open(player: Player) {
        val title = configuration.getString("title", " ")!!.colored()
        val sort = configuration.getStringList("sort").map { it.toCharArray().toList() }

        val basic = Basic(player, title)
        basic.rows(sort.size)
        basic.slots.addAll(sort)
        basic.onDrag { it.isCancelled = true }
        basic.onClick { it.isCancelled = true }

        val section = configuration.getConfigurationSection("items")

        if (section != null) {
            for (key in section.getKeys(false)) {
                if (key.length > 1) {
                    warn("buildMenu: $key is not a char")
                    continue
                }
                val internalName = section.getString("internal-name")
                val price = section.getDouble("price", 0.0)
                val commands = section.getStringList("commands")
                val reward = internalName?.let { Reward(it, price, commands) }
                if (reward != null) {
                    basic.onClick(key[0]) {
                        reward.sendTo(player)
                    }
                }
                basic.set(
                    key[0], section.convertItem(
                        player,
                        key,
                        if (reward != null) listOf(Variable("%state%", reward.preSendState(player))) else emptyList()
                    )
                )
            }
        }

        basic.openAsync()
    }

}