package com.xbaimiao.easypay.reward

import com.xbaimiao.easylib.chat.BuiltInConfiguration
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.ui.Variable
import com.xbaimiao.easylib.ui.convertItem
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.database.Database
import org.bukkit.entity.Player

object RewardHandle {

    private lateinit var configuration: BuiltInConfiguration
    val rewardsArgNode = ArgNode("reward", exec = { token ->
        getRewards().map { it.internalName }.filter { it.startsWith(token) }
    }, parse = { token ->
        getRewards().find { it.internalName == token }
    })

    fun loadConfiguration() {
        configuration = BuiltInConfiguration("reward.yml")
    }

    fun getRewards(): Collection<Reward> {
        val section = configuration.getConfigurationSection("items") ?: return emptyList()

        val collection = arrayListOf<Reward>()
        for (key in section.getKeys(false)) {
            val internalName = section.getString("$key.internal-name")
            val price = section.getDouble("$key.price", 0.0)
            val commands = section.getStringList("$key.commands")
            val reward = internalName?.let { Reward(it, price, commands, section.getString("$key.permission")) }
            if (reward != null) {
                collection.add(reward)
            }
        }
        return collection
    }

    fun open(player: Player) {
        launchCoroutine {
            val title = configuration.getString("title", " ")!!.colored()
            val sort = configuration.getStringList("sort").map { it.toCharArray().toList() }

            val basic = SpigotBasic(player, title)
            basic.rows(sort.size)
            basic.slots.addAll(sort)
            basic.onDrag { it.isCancelled = true }
            basic.onClick { it.isCancelled = true }


            val section = configuration.getConfigurationSection("items")
            val varTotal = Variable("%total%", Database.inst().getAllOrder(player.name).sumOf { it.price }.toString())

            if (section != null) {
                for (key in section.getKeys(false)) {
                    if (key.length > 1) {
                        warn("buildMenu: $key is not a char")
                        continue
                    }
                    val internalName = section.getString("$key.internal-name")
                    val price = section.getDouble("$key.price", 0.0)
                    val commands = section.getStringList("$key.commands")

                    val currentVars = arrayListOf(varTotal)

                    val reward = internalName?.let { Reward(it, price, commands, section.getString("$key.permission")) }
                    if (reward != null) {
                        basic.onClick(key[0]) {
                            launchCoroutine {
                                reward.sendReward(player)
                                switchContext(SynchronizationContext.SYNC)
                                open(player)
                            }
                        }
                    }
                    if (reward != null) {
                        currentVars.add(Variable("%state%", reward.preSendState(player)))
                    }
                    basic.set(key[0], section.convertItem(player, key, currentVars))
                }
            }

            basic.openAsync()
        }
    }

}
