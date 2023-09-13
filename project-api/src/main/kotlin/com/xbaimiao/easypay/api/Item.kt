package com.xbaimiao.easypay.api

import org.bukkit.entity.Player

/**
 * Item
 *
 * @author xbaimiao
 * @since 2023/9/12 23:01
 */
interface Item {

    /**
     * 商品价格
     */
    val price: Double

    /**
     * 商品名称
     */
    val name: String

    /**
     * 发货
     */
    fun sendTo(player: Player)

}