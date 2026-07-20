package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.buildMap
import com.xbaimiao.easylib.util.registerListener
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import java.awt.image.BufferedImage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object VirtualMap : MapUtil, Listener {

    private val closeCallbacks = ConcurrentHashMap<UUID, MutableSet<Runnable>>()
    @Volatile
    private var mainHand = true

    @Volatile
    override var cancelOnDrop = false
        private set

    init {
        registerListener(this)
    }

    fun configure(mainHand: Boolean, cancelOnDrop: Boolean) {
        this.mainHand = mainHand
        this.cancelOnDrop = cancelOnDrop
    }

    override fun clearAllMap(player: Player) {
        closeCallbacks.remove(player.uniqueId)
        player.updateInventory()
    }

    fun clearAllMaps() {
        val players = closeCallbacks.keys.mapNotNull(Bukkit::getPlayer)
        closeCallbacks.clear()
        players.forEach(Player::updateInventory)
    }

    @EventHandler(ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        if (!closeCallbacks.containsKey(event.player.uniqueId)) return
        event.isCancelled = true
        closeFromPlayer(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSlotChange(event: PlayerItemHeldEvent) {
        if (!closeCallbacks.containsKey(event.player.uniqueId)) return
        closeFromPlayer(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        if (!closeCallbacks.containsKey(event.player.uniqueId)) return
        event.isCancelled = true
        closeFromPlayer(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        closeFromPlayer(event.player, restoreInventory = false)
    }

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: Runnable) {
        closeCallbacks.computeIfAbsent(player.uniqueId) { ConcurrentHashMap.newKeySet() }.add(onDrop)

        val map = buildMap(bufferedImage, 128, 128)
        val equipmentSlot = if (mainHand) EquipmentSlot.HAND else EquipmentSlot.OFF_HAND

        player.sendEquipmentChange(player, equipmentSlot, map.mapItem)
        player.sendMap(map.mapView)
    }

    private fun closeFromPlayer(player: Player, restoreInventory: Boolean = true) {
        val callbacks = closeCallbacks.remove(player.uniqueId) ?: return
        if (restoreInventory) {
            player.updateInventory()
        }
        callbacks.forEach { it.run() }
    }
}
