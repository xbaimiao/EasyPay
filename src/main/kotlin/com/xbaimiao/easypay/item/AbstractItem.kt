package com.xbaimiao.easypay.item

import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.scripting.GroovyScriptManager
import com.xbaimiao.easypay.util.FunctionUtil
import org.bukkit.entity.Player

/**
 * AbstractItem
 *
 * @author xbaimiao
 * @since 2023/9/19 12:59
 */
abstract class AbstractItem(
    private val actions: List<String>,
    private val preActions: List<String>
) : Item {

    override fun preCreate(player: Player, service: PayService, order: Order): Boolean {
        return GroovyScriptManager.instance.preCreateOrder(
            player,
            service,
            order
        ) && FunctionUtil.instance.parseActions(player, order, service, preActions)
    }

    override fun onCreate(player: Player, service: PayService, order: Order) {
        GroovyScriptManager.instance.createOrder(player, service, order)
        FunctionUtil.instance.parseActions(player, order, service, actions)
    }

}