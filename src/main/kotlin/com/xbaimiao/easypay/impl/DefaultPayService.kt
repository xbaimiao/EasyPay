package com.xbaimiao.easypay.impl

import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.entity.PayService
import java.util.*
import java.util.concurrent.CompletableFuture

interface DefaultPayService : PayService {
    override fun createOrderCall(
        item: Item,
        call: Order.() -> Unit,
        timeout: Order.() -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Optional<Order>> {
        val future = CompletableFuture<Optional<Order>>()
        schedule {
            val orderOptional = async {
                createOrder(item)
            }
            if (!orderOptional.isPresent) {
                cancel.invoke()
                return@schedule
            }
            val order = orderOptional.get()
            future.complete(orderOptional)
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