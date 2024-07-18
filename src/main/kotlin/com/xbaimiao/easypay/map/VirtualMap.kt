package com.xbaimiao.easypay.map

import com.comphenix.packetwrapper.wrappers.play.clientbound.WrapperPlayServerMap
import com.comphenix.packetwrapper.wrappers.play.clientbound.WrapperPlayServerSetSlot
import com.comphenix.packetwrapper.wrappers.play.serverbound.WrapperPlayClientBlockDig
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.buildMap
import com.xbaimiao.easylib.util.registerListener
import com.xbaimiao.easypay.map.MapHelper.getMapId
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.awt.image.BufferedImage

class VirtualMap(private val mainHand: Boolean, override val cancelOnDrop: Boolean) : MapUtil, Listener {

    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
    private val dropFuncMap = mutableMapOf<String, MutableCollection<() -> Unit>>()

    init {
        registerListener(this)
        protocolManager.addPacketListener(object : PacketAdapter(
            EasyPlugin.getPlugin(),
            ListenerPriority.LOW,
            PacketType.Play.Client.BLOCK_DIG
        ) {
            override fun onPacketReceiving(event: PacketEvent) {
                if (event.packetType == PacketType.Play.Client.BLOCK_DIG) {
                    val wrapper = WrapperPlayClientBlockDig(event.packet)
                    if (wrapper.action == EnumWrappers.PlayerDigType.DROP_ITEM) {
                        event.player.updateInventory()
                        dropFuncMap.remove(event.player.name)?.forEach { it.invoke() }
                    }
                }
            }

        })
    }

    override fun clearAllMap(player: Player) {
        player.updateInventory()
    }

    @EventHandler
    fun onSlotChange(event: PlayerItemHeldEvent) {
        val player = event.player
        player.updateInventory()
        dropFuncMap.remove(player.name)?.forEach { it.invoke() }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        player.updateInventory()
        dropFuncMap.remove(player.name)?.forEach { it.invoke() }
    }

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit) {
        dropFuncMap.computeIfAbsent(player.name) { mutableSetOf() }.add(onDrop)
        // map
        val map = buildMap(bufferedImage, 125, 128)
        val mapItem = map.mapItem
        val mapId = map.getMapId()
        val mapView = map.mapView
        val render = mapView.javaClass.getDeclaredMethod("render", player.javaClass).invoke(mapView, player)
        val buffer = render.javaClass.getDeclaredField("buffer")[render] as ByteArray
        val mapWrapper = WrapperPlayServerMap()
        mapWrapper.mapId = mapId
        mapWrapper.decorations = arrayListOf()
        mapWrapper.colorPatch = WrapperPlayServerMap.WrappedMapPatch(0, 0, 128, 128, buffer)
        // fake item
        val itemWrapper = WrapperPlayServerSetSlot()
        itemWrapper.containerId = 0
        itemWrapper.stateId = 1
        itemWrapper.slot = if (mainHand) player.inventory.heldItemSlot + 36 else 45
        itemWrapper.itemStack = mapItem
        // send packets
        itemWrapper.sendPacket(player)
        mapWrapper.sendPacket(player)
    }
}