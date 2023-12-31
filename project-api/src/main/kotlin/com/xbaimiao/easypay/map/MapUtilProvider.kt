package com.xbaimiao.easypay.map

import com.xbaimiao.easylib.util.info

object MapUtilProvider {

    private var mapUtil: MapUtil? = null

    fun setMapUtil(mapUtil: MapUtil) {
        this.mapUtil = mapUtil
        info("使用 ${mapUtil.javaClass.simpleName} 做为地图工具")
    }

    fun getMapUtil(): MapUtil {
        return mapUtil!!
    }

}