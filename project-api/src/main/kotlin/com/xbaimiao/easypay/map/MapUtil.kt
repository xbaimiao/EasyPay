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

    val cancelOnDrop: Boolean

    fun clearAllMap(player: Player)

    /**
     * 给玩家发送一个地图物品，当玩家丢弃时执行 onDrop
     * @param player 玩家
     * @param bufferedImage 图片
     * @param onDrop 丢弃时执行
     */
    fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: () -> Unit = {})

}