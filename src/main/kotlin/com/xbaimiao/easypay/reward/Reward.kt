package com.xbaimiao.easypay.reward

import com.xbaimiao.easylib.bridge.player.parseECommand
import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easypay.database.Database
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
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
    val internalName: String, val price: Double, val commands: List<String>, val permission: String?
) {

    suspend fun preSendState(player: Player): String = suspendCoroutine {
        if (permission != null && !player.hasPermission(permission)) {
            it.resume(Lang.asLangText("reward-not-permission"))
            return@suspendCoroutine
        }
        launchCoroutine(SynchronizationContext.ASYNC) {
            val allPrice = Database.inst().getAllOrder(player.name).sumOf { it.price }
            if (allPrice < price) {
                it.resume(Lang.asLangText("reward-amount-not-reached"))
                return@launchCoroutine
            }
            if (!Database.inst().canGetReward(player.name, internalName)) {
                it.resume(Lang.asLangText("reward-have-already-received-it"))
                return@launchCoroutine
            }
            it.resume(Lang.asLangText("reward-can-be-claimed"))
        }
    }

    suspend fun sendReward(player: Player): Boolean = suspendCoroutine { continuation ->
        if (permission != null && !player.hasPermission(permission)) {
            player.sendLang("reward-not-permission-tips")
            continuation.resume(false)
            return@suspendCoroutine
        }
        launchCoroutine(SynchronizationContext.ASYNC) {
            val result = mutex.withLock {
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
                commands.parseECommand(player).exec(Bukkit.getConsoleSender())
            }
            continuation.resume(result)
        }
    }

    companion object {
        private val mutex: Mutex = Mutex()
    }

}
