package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.*
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.awt.image.BufferedImage

/**
 * RealMap
 *
 * @author xbaimiao
 * @since 2023/11/15 20:36
 */
class RealMap(private val mainHand: Boolean, override val cancelOnDrop: Boolean) : MapUtil, Listener {

    private val dropFuncMap = mutableMapOf<String, MutableCollection<() -> Unit>>()

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
        dropFuncMap.remove(event.player.name)?.forEach { it.invoke() }
    }

    @EventHandler
    fun hand(event: PlayerItemHeldEvent) {
        val player = event.player
        if (player.inventory.getItem(event.previousSlot).tryRemove(true)) {
            dropFuncMap.remove(player.name)?.forEach { it.invoke() }
            player.inventory.setItem(event.previousSlot, null)
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        val player = event.player
        if (player.inventory.itemInMainHand.tryRemove(true)) {
            dropFuncMap.remove(player.name)?.forEach { it.invoke() }
            player.inventory.itemInMainHand = null
        }
    }

    private fun ItemStack?.tryRemove(force: Boolean): Boolean {
        if (this == null) return false
        if (isNotAir()) {
            val nbt = NBTItem(this)
            if (nbt.hasKey("EasyPayRealMap")) {
                if (force) {
                    modifyMeta<ItemMeta> {
                        type = Material.AIR
                    }
                    amount = 0
                    return true
                }
                val generateTime = if (nbt.hasKey("EasyPayRealMapTime")) {
                    nbt.getDouble("EasyPayRealMapTime")
                } else {
                    0.0
                }
                if (generateTime == 0.0 || generateTime < System.currentTimeMillis()) {
                    modifyMeta<ItemMeta> {
                        type = Material.AIR
                    }
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

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit) {
        dropFuncMap.computeIfAbsent(player.name) { mutableSetOf() }.add(onDrop)
        var mapItem = buildMap(bufferedImage, 128, 128).mapItem

        val nbt = NBTItem(mapItem)
        nbt.setInteger("EasyPayRealMap", 63)
        nbt.setDouble("EasyPayRealMapTime", System.currentTimeMillis().toDouble() + (1000 * 60 * 5))
        mapItem = nbt.item

        if (mainHand) {
            var itemStack: ItemStack? = null
            if (player.inventory.itemInMainHand.isNotAir()) {
                itemStack = player.inventory.itemInMainHand.clone()
            }
            player.inventory.itemInMainHand = mapItem
            if (itemStack != null) {
                player.giveItem(itemStack)
            }
        } else {
            var itemStack: ItemStack? = null
            if (player.inventory.itemInOffHand.isNotAir()) {
                itemStack = player.inventory.itemInOffHand.clone()
            }
            player.inventory.itemInOffHand = mapItem
            if (itemStack != null) {
                player.giveItem(itemStack)
            }
        }
    }

}