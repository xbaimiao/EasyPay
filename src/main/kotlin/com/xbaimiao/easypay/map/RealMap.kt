package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.*
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
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
                onlinePlayer.inventory.forEach { it.tryRemove(false) }
            }
        }
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        if (event.itemDrop.itemStack.tryRemove(true) || event.player.inventory.itemInMainHand.tryRemove(true)) {
            event.itemDrop.remove()
            event.isCancelled = true
        }
        dropFuncMap.remove(event.player.name)?.forEach { it.invoke() }
    }

    @EventHandler
    fun hand(event: PlayerItemHeldEvent) {
        val player = event.player
        if (player.inventory.getItem(event.previousSlot).tryRemove(true)) {
            dropFuncMap.remove(player.name)?.forEach { it.invoke() }
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        check(event.player)
    }

    @EventHandler
    fun i(event: PlayerInteractAtEntityEvent) {
        check(event.player)
    }

    @EventHandler
    fun b(event: PlayerInteractEvent) {
        if (event.clickedBlock != null) {
            check(event.player)
        }
    }

    private fun check(player: Player) {
        if (player.inventory.itemInMainHand.tryRemove(true)) {
            dropFuncMap.remove(player.name)?.forEach { it.invoke() }
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
        player.inventory.forEach { it.tryRemove(true) }
        player.updateInventory()
    }

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit) {
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