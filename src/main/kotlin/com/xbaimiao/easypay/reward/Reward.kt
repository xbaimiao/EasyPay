package com.xbaimiao.easypay.reward

import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.util.eu.parseECommand
import com.xbaimiao.easypay.database.Database
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Reward
 *
 * @author xbaimiao
 * @since 2023/10/13 23:52
 */
class Reward(
    private val internalName: String,
    private val price: Double,
    private val commands: List<String>
) {

    fun preSendState(player: Player): String {
        val allPrice = Database.inst().getAllOrder(player.name).sumOf { it.price }
        if (allPrice < price) {
            return Lang.asLangText("reward-amount-not-reached")
        }
        if (!Database.inst().canGetReward(player.name, internalName)) {
            return Lang.asLangText("reward-have-already-received-it")
        }
        return Lang.asLangText("reward-can-be-claimed")
    }

    fun sendTo(player: Player) {
        val allPrice = Database.inst().getAllOrder(player.name).sumOf { it.price }
        if (allPrice < price) {
            player.sendLang("this-amount-has-not-been-reached")
            return
        }
        if (!Database.inst().canGetReward(player.name, internalName)) {
            player.sendLang("already-received-it")
            return
        }
        Database.inst().setGetReward(player.name, internalName)
        commands.parseECommand(player).exec(Bukkit.getConsoleSender())
    }

}