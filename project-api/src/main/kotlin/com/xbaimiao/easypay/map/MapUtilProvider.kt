package com.xbaimiao.easypay.map

object MapUtilProvider {

    private var mapUtil: MapUtil? = null

    fun setMapUtil(mapUtil: MapUtil) {
        this.mapUtil = mapUtil
    }

    fun getMapUtil(): MapUtil {
        return mapUtil!!
    }

}