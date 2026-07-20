package com.xbaimiao.easypay.map

import org.bukkit.entity.Player
import java.awt.image.BufferedImage

/**
 * MapUtil
 *
 * @author xbaimiao
 * @since 2023/9/19 08:08
 */
interface MapUtil {

    /**
     * 是否在关闭虚拟地图时取消订单
     */
    val cancelOnDrop: Boolean

    /**
     * 清除玩家正在查看的虚拟地图
     */
    fun clearAllMap(player: Player)

    /**
     * 向玩家显示虚拟地图，玩家关闭时执行 onDrop
     * @param player 玩家
     * @param bufferedImage 图片
     * @param onDrop 丢弃时执行
     */
    fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: Runnable = Runnable { })

}
