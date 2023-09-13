package com.xbaimiao.easypay.impl

import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.entity.PayService
import java.util.concurrent.CompletableFuture

interface DefaultPayService : PayService {
    override fun createOrderCall(
        item: Item,
        call: Order.() -> Unit,
        timeout: Order.() -> Unit
    ): CompletableFuture<Order> {
        val future = CompletableFuture<Order>()
        schedule {
            val order = async {
                createOrder(item)
            }
            if (order.orderId.equals("dupe", ignoreCase = true)) {
                timeOut(timeout, order)
                return@schedule
            }
            future.complete(order)
            // 查询5分钟 查询一次等待1秒
            for (index in 0..(60 * 5)) {
                val status = async {
                    queryOrder(order)
                }
                // 如果已经支付跳出循环调用回调方法
                if (status == OrderStatus.SUCCESS) {
                    call.invoke(order)
                    return@schedule
                }
                // 等待1秒在查询
                waitFor(20)
            }
            timeOut(timeout, order)
        }
        return future
    }

    fun timeOut(timeout: Order.() -> Unit, order: Order)
}