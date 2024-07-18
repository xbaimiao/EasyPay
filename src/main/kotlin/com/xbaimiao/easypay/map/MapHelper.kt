package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.MapItem
import org.bukkit.inventory.meta.MapMeta

object MapHelper {
    fun MapItem.getMapId(): Int {
        val mapView = this.mapView
        try {
            val idMethod = mapView.javaClass.getDeclaredMethod("getId", Int::class.java)
            return idMethod.invoke(mapView) as Int
        } catch (e: Exception) {
            val mapItem = this.mapItem
            val mapMeta = mapItem.itemMeta!! as MapMeta
            val mapIdMethod = mapMeta.javaClass.getMethod("getMapId")
            mapIdMethod.isAccessible = true
            return mapIdMethod.invoke(mapMeta) as Int
        }
    }
}