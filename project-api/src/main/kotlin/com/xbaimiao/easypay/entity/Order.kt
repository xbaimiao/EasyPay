package com.xbaimiao.easypay.entity

import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.database.WebOrder

/**
 * Order
 *
 * @author xbaimiao
 * @since 2023/9/12 23:07
 */
data class Order(
    val orderId: String, val item: Item, val qrCode: String, val service: PayService, var price: Double
) {

    fun close() {
        service.close(this)
    }

    fun baseWebOrder(player: String): WebOrder {
        return WebOrder(
            System.currentTimeMillis(),
            0,
            0,
            item.name,
            orderId,
            service.name,
            item.price,
            player,
            WebOrder.Status.WAIT,
            ""
        )
    }

}