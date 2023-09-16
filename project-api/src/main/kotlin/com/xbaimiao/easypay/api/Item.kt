package com.xbaimiao.easypay.api

import com.xbaimiao.easypay.entity.PayService
import org.bukkit.entity.Player
import java.io.Serializable

/**
 * Item
 *
 * @author xbaimiao
 * @since 2023/9/12 23:01
 */
interface Item : Serializable {

    /**
     * 商品价格
     */
    var price: Double

    /**
     * 商品名称
     */
    val name: String

    /**
     * 预创建判断
     */
    fun preCreate(player: Player, service: PayService): Boolean

    /**
     * 创建订单成功后执行
     */
    fun onCreate(player: Player, service: PayService)

    /**
     * 发货
     */
    fun sendTo(player: Player, service: PayService)

}