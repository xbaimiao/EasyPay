package com.xbaimiao.easypay.api

import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import org.bukkit.entity.Player

abstract class AbstractScriptingExtension {

    /**
     * 订单完成向玩家方法奖励时执行
     */
    abstract fun orderReward(player: Player, service: PayService, order: Order)

    /**
     * 订单预创建时执行
     * @return 是否创建订单
     */
    abstract fun preCreateOrder(player: Player, service: PayService, order: Order): Boolean

    /**
     * 订单创建时执行
     */
    abstract fun createOrder(player: Player, service: PayService, order: Order)
}