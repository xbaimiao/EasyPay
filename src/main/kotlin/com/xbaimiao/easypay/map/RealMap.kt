package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.*
import de.tr7zw.changeme.nbtapi.NBTItem
import kotlinx.coroutines.Runnable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import java.awt.image.BufferedImage

/**
 * RealMap
 *
 * @author xbaimiao
 * @since 2023/11/15 20:36
 */
class RealMap(private val mainHand: Boolean, override val cancelOnDrop: Boolean) : MapUtil, Listener {

    private val dropFuncMap = mutableMapOf<String, MutableCollection<java.lang.Runnable>>()

    init {
        registerListener(this)
        submit(period = 20) {
            for (onlinePlayer in onlinePlayers()) {
                onlinePlayer.inventory.withIndex().forEach { (slot, it) ->
                    if (it.tryRemove(false)) {
                        onlinePlayer.inventory.setItem(slot, null)
                    }
                }
            }
        }
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        if (event.itemDrop.itemStack.tryRemove(true) || event.player.inventory.itemInMainHand.tryRemove(true)) {
            submit(delay = 3) {
                event.itemDrop.remove()
            }
        }
        dropFuncMap.remove(event.player.name)?.forEach { it.run() }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        val player = event.player
        if (player.inventory.itemInMainHand.tryRemove(true)) {
            dropFuncMap.remove(player.name)?.forEach { it.run() }
            player.inventory.setItemInMainHand(null)
        }
    }

    private fun ItemStack?.tryRemove(force: Boolean): Boolean {
        if (this == null) return false
        if (isNotAir()) {
            val nbt = NBTItem(this)
            if (nbt.hasTag("EasyPayRealMap")) {
                if (force) {
                    amount = 0
                    return true
                }
                val generateTime = if (nbt.hasTag("EasyPayRealMapTime")) {
                    nbt.getLong("EasyPayRealMapTime")
                } else {
                    0
                }
                if (generateTime == 0L || generateTime < System.currentTimeMillis()) {
                    amount = 0
                    return true
                }
                return false
            }
        }
        return false
    }

    override fun clearAllMap(player: Player) {
        player.inventory.withIndex().forEach { (slot, it) ->
            if (it.tryRemove(true)) {
                player.inventory.setItem(slot, null)
            }
        }
    }

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: Runnable) {
        dropFuncMap.computeIfAbsent(player.name) { mutableSetOf() }.add(onDrop)
        val mapItem = buildMap(bufferedImage, 128, 128).mapItem

        val nbt = NBTItem(mapItem)
        nbt.setInteger("EasyPayRealMap", 63)
        nbt.setLong("EasyPayRealMapTime", System.currentTimeMillis() + (1000 * 60 * 5))
        nbt.applyNBT(mapItem)

        if (mainHand) {
            var itemStack: ItemStack? = null
            if (player.inventory.itemInMainHand.isNotAir()) {
                itemStack = player.inventory.itemInMainHand.clone()
            }
            player.inventory.setItemInMainHand(mapItem)
            if (itemStack != null) {
                player.giveItem(itemStack)
            }
        } else {
            var itemStack: ItemStack? = null
            if (player.inventory.itemInOffHand.isNotAir()) {
                itemStack = player.inventory.itemInOffHand.clone()
            }
            player.inventory.setItemInOffHand(mapItem)
            if (itemStack != null) {
                player.giveItem(itemStack)
            }
        }
    }

}
