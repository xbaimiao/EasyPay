package com.xbaimiao.easypay.database

import com.xbaimiao.easypay.entity.Order

/**
 * OrderData
 *
 * @author xbaimiao
 * @since 2023/9/18 19:05
 */
data class OrderData(
    val orderId: String,
    val item: String,
    val qrCode: String,
    val service: String,
    val price: Double,
    val playName: String
) {

    companion object {
        fun fromOrder(order: Order, playName: String): OrderData = OrderData(
            order.orderId,
            order.item.name,
            order.qrCode,
            order.service,
            order.price,
            playName
        )
    }

}