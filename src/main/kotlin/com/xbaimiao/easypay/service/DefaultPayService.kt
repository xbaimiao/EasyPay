package com.xbaimiao.easypay.service

import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.WebOrder
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.entity.PayService
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue

interface DefaultPayService : PayService {

    companion object {
        private val closeOrderList = ConcurrentLinkedQueue<String>()
    }

    override fun createOrderCall(
        player: Player,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Order?> {
        return createOrderCall(player, item, call, timeout, cancel, 60 * 5)
    }

    override fun close(order: Order) {
        closeOrderList.add(order.orderId)
    }

    fun createOrderCall(
        player: Player,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit,
        waitTime: Int
    ): CompletableFuture<Order?> {
        val future = CompletableFuture<Order?>()
        launchCoroutine {
            val order = async {
                createOrder(player, item)?.also {
                    Database.inst().addWebOrder(it.baseWebOrder(player.name))
                }
            }
            if (order == null) {
                debug("订单创建失败 ${this@DefaultPayService::class.java.simpleName}")
                cancel.invoke()
                return@launchCoroutine
            }
            future.complete(order)
            // 查询5分钟 查询一次等待1秒
            for (index in 0..(waitTime)) {
                val close = async {
                    closeOrderList.remove(order.orderId)
                }
                if (close) {
                    debug("订单被关闭 ${order.orderId}")
                    return@launchCoroutine
                }
                val status = async {
                    debug("查询订单 ${order.orderId}")
                    queryOrder(order)
                }
                debug("订单状态: $status")
                // 如果已经支付跳出循环调用回调方法
                if (status == OrderStatus.SUCCESS) {
                    async {
                        val webOrder = Database.inst().getWebOrder(order.orderId)
                        if (webOrder != null) {
                            webOrder.status = WebOrder.Status.SUCCESS
                            webOrder.payTime = System.currentTimeMillis()
                            Database.inst().updateWebOrder(webOrder)
                        }
                    }
                    debug("支付成功 ${order.orderId}")
                    call.invoke(this, order)
                    return@launchCoroutine
                }
                // 等待1秒在查询
                waitFor(20)
            }
            debug("订单超时 ${order.orderId}")
            timeOut(timeout, order)
        }
        return future
    }

    fun timeOut(timeout: suspend SchedulerController.(Order) -> Unit, order: Order) {
        launchCoroutine {
            timeout.invoke(this, order)
        }
    }

}