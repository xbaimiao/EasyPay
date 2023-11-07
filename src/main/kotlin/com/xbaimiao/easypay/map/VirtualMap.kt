package com.xbaimiao.easypay.map

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.xbaimiao.easylib.nms.NMSMap
import com.xbaimiao.easylib.nms.sendMap
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import org.bukkit.entity.Player
import java.awt.image.BufferedImage

/**
 * VirtualMap
 *
 * @author xbaimiao
 * @since 2023/9/19 08:11
 */
class VirtualMap(private val hand: NMSMap.Hand, override val cancelOnDrop: Boolean) : MapUtil {

    companion object {
        private val listener: Any by lazy {
            PacketEvents.getAPI().eventManager.registerListener(
                VirtualMapListener,
                PacketListenerPriority.LOW
            )
        }
        private val dropFuncMap = mutableMapOf<String, MutableCollection<() -> Unit>>()
    }

    private object VirtualMapListener : PacketListener {

        override fun onPacketReceive(event: PacketReceiveEvent) {
            if (event.packetType == PacketType.Play.Client.PLAYER_DIGGING) {
                val wrapper = WrapperPlayClientPlayerDigging(event)
                if (wrapper.action.name.contains("DROP")) {
                    runCatching {
                        val player = (event.player as Player).name
                        dropFuncMap.remove(player)?.forEach { it.invoke() }
                    }
                }
            }
        }

    }

    init {
        runCatching {
            info("注册虚拟地图监听 $listener")
        }.onFailure {
            warn("注册虚拟地图监听失败 丢弃地图取消订单功能将无法使用")
        }
    }

    override fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit) {
        dropFuncMap.computeIfAbsent(player.name) { mutableSetOf() }.add(onDrop)
        player.sendMap(bufferedImage, hand)
    }

}