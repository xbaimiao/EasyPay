package com.xbaimiao.easypay.service

import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.entity.PayService
import java.util.*
import java.util.concurrent.CompletableFuture

interface DefaultPayService : PayService {

    override fun createOrderCall(
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Optional<Order>> {
        val future = CompletableFuture<Optional<Order>>()
        schedule {
            val orderOptional = async {
                createOrder(item)
            }
            if (!orderOptional.isPresent) {
                debug("订单创建失败 ${this@DefaultPayService::class.java.simpleName}")
                cancel.invoke()
                return@schedule
            }
            val order = orderOptional.get()
            future.complete(orderOptional)
            // 查询5分钟 查询一次等待1秒
            for (index in 0..(60 * 5)) {
                val status = async {
                    debug("查询订单 ${order.orderId}")
                    queryOrder(order)
                }
                debug("订单状态: $status")
                // 如果已经支付跳出循环调用回调方法
                if (status == OrderStatus.SUCCESS) {
                    debug("支付成功 ${order.orderId}")
                    call.invoke(this, order)
                    return@schedule
                }
                // 等待1秒在查询
                waitFor(20)
            }
            debug("订单超时 ${order.orderId}")
            timeOut(timeout, order)
        }
        return future
    }

    fun timeOut(timeout: suspend SchedulerController.(Order) -> Unit, order: Order)

}