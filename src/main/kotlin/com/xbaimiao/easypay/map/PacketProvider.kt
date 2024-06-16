package com.xbaimiao.easypay.map

import org.bukkit.Bukkit

enum class PacketProvider(val plugin: String, val provider: Class<out MapUtil>) {
    ProtocolLib("ProtocolLib", VirtualMap::class.java),
    PacketEvents("PacketEvents", PacketEventsVirtualMap::class.java);

    fun getMapUtil(mainHand: Boolean, cancelOnDrop: Boolean): MapUtil {
        if (!Bukkit.getPluginManager()
                .isPluginEnabled(plugin)
        ) throw IllegalStateException("Packet provider not loaded: $plugin")
        return provider
            .getDeclaredConstructor(Boolean::class.java, Boolean::class.java)
            .newInstance(mainHand, cancelOnDrop)
    }
}