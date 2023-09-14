package com.xbaimiao.easypay.entity

import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easypay.api.Item
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt

/**
 * PayService
 *
 * @author xbaimiao
 * @since 2023/9/12 23:04
 */
interface PayService {

    val name: String

    /**
     * 创建一个订单 此方法为同步创建 会占用主线程资源 推荐使用 [createOrderCall]
     */
    fun createOrder(player: Player, item: Item): Optional<Order>

    /**
     * 创建一个订单并在支付完成后回调
     * @param player 玩家
     * @param item 订单商品
     * @param call 支付完成的回调
     * @param timeout 支付超时后回调
     * @param cancel 订单被取消后回调
     *
     * @return CompletableFuture<Order> 订单创建完成时 complete 而不是支付完成
     */
    fun createOrderCall(
        player: Player,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Optional<Order>>

    /**
     * 查询订单状态 此方法为同步查询 会占用主线程资源 推荐异步调用 或使用 [createOrderCall]
     */
    fun queryOrder(order: Order): OrderStatus

    fun generateOrderId(): String {
        return (System.currentTimeMillis() + (Math.random() * 10000000L).roundToInt()).toString()
    }

}