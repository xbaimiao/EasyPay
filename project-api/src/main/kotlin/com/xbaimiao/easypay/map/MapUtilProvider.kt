package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.info

object MapUtilProvider {

    private var mapUtil: MapUtil? = null
    private var final = false

    fun setMapUtil(mapUtil: MapUtil) {
        if (final && this.mapUtil != null) {
            return
        }
        this.mapUtil = mapUtil
        info("使用 ${mapUtil.javaClass.simpleName} 做为地图工具")
    }

    fun getMapUtil(): MapUtil {
        return mapUtil!!
    }

    fun setFinalMapUtil(mapUtil: MapUtil) {
        this.mapUtil = mapUtil
        this.final = true
        info("使用 ${mapUtil.javaClass.simpleName} 做为地图工具 [final]")
    }

}