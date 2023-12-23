package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.nms.NMSMap
import com.xbaimiao.easylib.nms.buildMap
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
class RealMap(private val hand: NMSMap.Hand, override val cancelOnDrop: Boolean) : MapUtil, Listener {

    private val dropFuncMap = mutableMapOf<String, MutableCollection<() -> Unit>>()

    init {
        registerListener(this)
        submit(period = 20) {
            for (onlinePlayer in onlinePlayers()) {
                onlinePlayer.inventory.forEach { it.tryRemove() }
            }
        }
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        dropFuncMap.remove(event.player.name)?.forEach { it.invoke() }
    }

    @EventHandler
    fun hand(event: PlayerItemHeldEvent) {
        val player = event.player
        if (player.inventory.getItem(event.previousSlot).tryRemove()) {
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
        if (player.inventory.itemInMainHand.tryRemove()) {
            dropFuncMap.remove(player.name)?.forEach { it.invoke() }
        }
    }

    private fun ItemStack?.tryRemove(): Boolean {
        if (this == null) return false
        if (isNotAir()) {
            val nbt = NBTItem(this)
            if (nbt.hasTag("EasyPayRealMap")) {
                amount = 0
                return true
            }
        }
        return false
    }

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit) {
        dropFuncMap.computeIfAbsent(player.name) { mutableSetOf() }.add(onDrop)
        val mapItem = buildMap(bufferedImage, hand, 128, 128).mapItem

        val nbt = NBTItem(mapItem)
        nbt.setInteger("EasyPayRealMap", 63)
        nbt.applyNBT(mapItem)

        if (hand == NMSMap.Hand.MAIN) {
            if (player.inventory.itemInMainHand.isNotAir()) {
                player.giveItem(player.inventory.itemInMainHand)
            }
            player.inventory.itemInMainHand = mapItem
        } else {
            if (player.inventory.itemInOffHand.isNotAir()) {
                player.giveItem(player.inventory.itemInOffHand)
            }
            player.inventory.itemInOffHand = mapItem
        }
    }

}