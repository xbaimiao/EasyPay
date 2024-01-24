package com.xbaimiao.easypay.item

import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import org.bukkit.entity.Player

data class ItemSack(
    val items: List<Item>,
    override val price: Double = items.sumOf { it.price },
    override val name: String = items.joinToString(", ") { it.name }
) : Item {
    override fun preCreate(player: Player, service: PayService, order: Order): Boolean {
        var currentBoolean = true
        for (item in items) {
            if (!currentBoolean) return false
            currentBoolean = item.preCreate(player, service, order)
        }
        return currentBoolean
    }

    override fun onCreate(player: Player, service: PayService, order: Order) {
        items.forEach {
            it.onCreate(player, service, order)
        }
    }

    override fun sendTo(player: Player, service: PayService, order: Order): Collection<String> {
        val list = mutableListOf<String>()
        for (item in items) {
            list.addAll(item.sendTo(player, service, order))
        }
        return list
    }
}
