package com.xbaimiao.easypay.service

import com.xbaimiao.easylib.util.ShortUUID
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import java.io.File

/**
 * @author xbaimiao
 * @date 2024/7/19
 * @email owner@xbaimiao.com
 */
object DevService : DefaultPayService {

    val waitOrders = ArrayList<Order>()
    val doneOrders = ArrayList<String>()

    /**
     * 离线创建一个订单 此方法为同步创建 会占用主线程资源 推荐使用 [createOrderCall]
     */
    override fun createOrder(player: String, item: Item): Order {
        return Order(
            ShortUUID.randomShortUUID().toString(),
            item,
            "sdiadncjkasdasdnmxzkjkjhdieuaodass",
            this,
            100.0
        ).also {
            waitOrders.add(it)
        }
    }

    override val name: String = "dev"
    override val displayName: String = "开发者调试"
    override val logoFile: File = ZxingUtil.wechatLogo

    /**
     * 查询订单状态 此方法为同步查询 会占用主线程资源 推荐异步调用 或使用 [createOrderCall]
     */
    override fun queryOrder(order: Order): OrderStatus {
        if (order.orderId in doneOrders) {
            return OrderStatus.SUCCESS
        }
        return OrderStatus.WAIT_PAY
    }

    /**
     * 可交互的 (即可以在网页上付款 而不需要扫描)
     */
    override fun isInteractive(): Boolean {
        return false
    }

}
