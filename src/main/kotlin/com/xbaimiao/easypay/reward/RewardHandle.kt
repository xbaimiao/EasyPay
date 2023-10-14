package com.xbaimiao.easypay.reward

import com.xbaimiao.easylib.chat.BuiltInConfiguration
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.ui.Variable
import com.xbaimiao.easylib.ui.convertItem
import com.xbaimiao.easylib.util.eu.parseECommand
import com.xbaimiao.easylib.util.lock.buildDistributedLock
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.database.Database
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object RewardHandle {

    private lateinit var configuration: BuiltInConfiguration
    private val distributedLock = buildDistributedLock()

    fun loadConfiguration() {
        configuration = BuiltInConfiguration("reward.yml")
    }

    fun open(player: Player) {
        schedule {
            val title = configuration.getString("title", " ")!!.colored()
            val sort = configuration.getStringList("sort").map { it.toCharArray().toList() }

            val basic = Basic(player, title)
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
                            if (reward.permission != null && !player.hasPermission(reward.permission)) {
                                player.sendLang("reward-not-permission-tips")
                                return@onClick
                            }
                            schedule(SynchronizationContext.ASYNC) {
                                val result = distributedLock.withLock("get-reward-${player.name}") {
                                    val allPrice = Database.inst().getAllOrder(player.name).sumOf { it.price }
                                    if (allPrice < price) {
                                        player.sendLang("reward-this-amount-has-not-been-reached")
                                        return@withLock false
                                    }
                                    if (!Database.inst().canGetReward(player.name, internalName)) {
                                        player.sendLang("reward-already-received-it")
                                        return@withLock false
                                    }
                                    Database.inst().setGetReward(player.name, internalName)
                                    return@withLock true
                                }
                                if (result) {
                                    switchContext(SynchronizationContext.SYNC)
                                    reward.commands.parseECommand(player).exec(Bukkit.getConsoleSender())
                                    open(player)
                                }
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