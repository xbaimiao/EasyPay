package com.xbaimiao.easypay.reward

import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easypay.database.Database
import org.bukkit.entity.Player
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Reward
 *
 * @author xbaimiao
 * @since 2023/10/13 23:52
 */
class Reward(
    val internalName: String,
    val price: Double,
    val commands: List<String>,
    val permission: String?
) {

    suspend fun preSendState(player: Player): String = suspendCoroutine {
        if (permission != null && !player.hasPermission(permission)) {
            it.resume(Lang.asLangText("reward-not-permission"))
            return@suspendCoroutine
        }
        schedule(SynchronizationContext.ASYNC) {
            val allPrice = Database.inst().getAllOrder(player.name).sumOf { it.price }
            if (allPrice < price) {
                it.resume(Lang.asLangText("reward-amount-not-reached"))
                return@schedule
            }
            if (!Database.inst().canGetReward(player.name, internalName)) {
                it.resume(Lang.asLangText("reward-have-already-received-it"))
                return@schedule
            }
            it.resume(Lang.asLangText("reward-can-be-claimed"))
        }
    }

}