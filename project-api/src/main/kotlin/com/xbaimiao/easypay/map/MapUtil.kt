package com.xbaimiao.easypay.map

import kotlinx.coroutines.Runnable
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
     * 是否在丢弃地图/关闭ui的时候关闭订单
     */
    val cancelOnDrop: Boolean

    /**
     * 请实现清除地图/关闭ui
     */
    fun clearAllMap(player: Player)

    /**
     * 给玩家发送一个 地图物品/或打开ui，当玩家 丢弃/关闭 时执行 onDrop
     * @param player 玩家
     * @param bufferedImage 图片
     * @param onDrop 丢弃时执行
     */
    fun sendMap(player: Player, bufferedImage: BufferedImage, onDrop: Runnable = Runnable { })

}