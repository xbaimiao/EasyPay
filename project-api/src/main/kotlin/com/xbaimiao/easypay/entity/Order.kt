package com.xbaimiao.easypay.entity

import com.xbaimiao.easypay.api.Item

/**
 * Order
 *
 * @author xbaimiao
 * @since 2023/9/12 23:07
 */
data class Order(
    val orderId: String,
    val item: Item,
    val qrCode: String,
    val service: String
) {

    /**
     * 从数据库获取的Order player不会为空
     */
    var player: String? = null

}