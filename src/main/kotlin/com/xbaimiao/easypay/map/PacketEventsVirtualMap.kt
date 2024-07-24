package com.xbaimiao.easypay.map

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData.MapDecoration
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.xbaimiao.easylib.util.buildMap
import com.xbaimiao.easylib.util.registerListener
import com.xbaimiao.easypay.map.MapHelper.getMapId
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.awt.image.BufferedImage


class PacketEventsVirtualMap(private val mainHand: Boolean, override val cancelOnDrop: Boolean) : MapUtil, Listener {

    private val packetEvents = PacketEvents.getAPI()
    private val dropFuncMap = mutableMapOf<String, MutableCollection<() -> Unit>>()
    //var buffer: ByteArray? = null

    init {
        registerListener(this)
        packetEvents.eventManager.registerListener(object : PacketListenerAbstract(
            PacketListenerPriority.LOW
        ) {
            override fun onPacketReceive(event: PacketReceiveEvent) {
                if (event.packetType == PacketType.Play.Client.PLAYER_DIGGING) {
                    val wrapper = WrapperPlayClientPlayerDigging(event)
                    val player = event.player as Player
                    if (wrapper.action == DiggingAction.DROP_ITEM) {
                        player.updateInventory()
                        dropFuncMap.remove(player.name)?.forEach { it.invoke() }
                    }
                }
            }

            /*override fun onPacketSend(event: PacketSendEvent) {
                if (event.packetType == PacketType.Play.Server.MAP_DATA) {
                    if (buffer == null) return
                    val wrapper = WrapperPlayServerMapData(event)
                    if (wrapper.rows == 128 && wrapper.columns == 128) {
                        wrapper.data = buffer
                        event.isCancelled = true
                        event.user.sendPacket(wrapper)
                    }
                }
            }*/
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

    /*private fun createBuffer(mapId: Int, pixels: ByteArray): ByteBuf {
        val buffer: ByteBuf = Unpooled.buffer()
        val version = packetEvents.serverManager.version
        ByteBufHelper.writeVarInt(buffer, PacketType.Play.Server.MAP_DATA.getId(version.toClientVersion())) //packed id
        ByteBufHelper.writeVarInt(buffer, mapId) //map id
        ByteBufHelper.writeByte(buffer, 0) //scale
        if (version.isNewerThanOrEquals(ServerVersion.V_1_9)
            && version.isOlderThan(ServerVersion.V_1_17)
        ) {
            ByteBufHelper.writeBoolean(buffer, false) //has icons/locked
        }
        if (version.isNewerThanOrEquals(ServerVersion.V_1_14)) {
            ByteBufHelper.writeBoolean(buffer, false) //locked/tracking positions
        }

        if (version.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            ByteBufHelper.writeBoolean(buffer, false)
        } else {
            ByteBufHelper.writeVarInt(buffer, 0)
        }

        ByteBufHelper.writeByte(buffer, 128) //updated columns
        ByteBufHelper.writeByte(buffer, 128) //updated rows
        ByteBufHelper.writeByte(buffer, 0) //x offset
        ByteBufHelper.writeByte(buffer, 0) //z offset
        ByteBufHelper.writeVarInt(
            buffer,
            pixels.size
        ) //byte array's length, so here 16384 (128x128, since the offsets are 0)
        ByteBufHelper.writeBytes(buffer, pixels) //write the byte array (map's pixels)
        return buffer
    }*/

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit) {
        dropFuncMap.computeIfAbsent(player.name) { mutableSetOf() }.add(onDrop)
        // map
        val map = buildMap(bufferedImage, 128, 128)
        val mapItem = map.mapItem
        val mapView = map.mapView
        val render = mapView.javaClass.getDeclaredMethod("render", player.javaClass).invoke(mapView, player)
        val buffer = render.javaClass.getDeclaredField("buffer")[render] as ByteArray
        val mapId = map.getMapId()
        val mapPacket =
            WrapperPlayServerMapData(mapId, 0, false, false, listOf<MapDecoration>(), 128, 128, 0, 0, buffer)
        // fake item
        val itemWrapper = WrapperPlayServerSetSlot(
            0,
            1,
            if (mainHand) player.inventory.heldItemSlot + 36 else 45,
            SpigotConversionUtil.fromBukkitItemStack(mapItem)
        )

        packetEvents.playerManager.sendPacket(player, itemWrapper)
        packetEvents.playerManager.sendPacket(player, mapPacket)
        //this.buffer = buffer
    }
}