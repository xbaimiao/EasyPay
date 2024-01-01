package com.xbaimiao.easypay.item

import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import org.bukkit.entity.Player

/**
 * AbstractItem
 *
 * @author xbaimiao
 * @since 2023/9/19 12:59
 */
abstract class AbstractItem : Item {

    override fun preCreate(player: Player, service: PayService, order: Order): Boolean {
        return true
    }

    override fun onCreate(player: Player, service: PayService, order: Order) {
    }

}