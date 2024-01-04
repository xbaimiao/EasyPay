package com.xbaimiao.easypay.api

import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
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
     * 预创建判断
     */
    fun preCreate(player: Player, service: PayService, order: Order): Boolean

    /**
     * 创建订单成功后执行
     */
    fun onCreate(player: Player, service: PayService, order: Order)

    /**
     * 发货
     */
    fun sendTo(player: Player, service: PayService?, order: Order): Collection<String>

}