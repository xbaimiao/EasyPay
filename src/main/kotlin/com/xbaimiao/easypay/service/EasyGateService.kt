package com.xbaimiao.easypay.service

import com.google.gson.JsonParser
import com.xbaimiao.easylib.skedule.SchedulerController
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import fuel.FuelBuilder
import fuel.Request
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.CompletableFuture


class EasyGateService(
    private val clientId: String,
    private val clientSecret: String,
    private val expireTime: Int
) : DefaultPayService {
    val fuel = FuelBuilder().build()
    private val baseUrl = "https://afdian.rgbmc.org/api"

    override fun createOrderCall(
        player: String,
        item: Item,
        call: suspend SchedulerController.(Order) -> Unit,
        timeout: suspend SchedulerController.(Order) -> Unit,
        cancel: () -> Unit
    ): CompletableFuture<Order?> {
        return createOrderCall(player, item, call, timeout, cancel, expireTime)
    }

    override fun createOrder(player: String, item: Item): Order? {
        return runBlocking {
            val response = fuel.get(
                request = Request.Builder()
                    .url("$baseUrl/order/create")
                    .headers(
                        mapOf(
                            "Client-ID" to clientId,
                            "Client-Secret" to clientSecret
                        )
                    )
                    .parameters(
                        listOf(
                            "price" to item.price.toString(),
                            "expireTime" to expireTime.toString()
                        )
                    )
                    .build()
            )
            if (response.statusCode != 200) return@runBlocking null
            val bodyText = response.body
            val jsonObject = JsonParser().parse(bodyText).asJsonObject
            val orderId = jsonObject["orderId"].asString
            val redirectUrl = jsonObject["url"].asString
            return@runBlocking Order(orderId, item, redirectUrl, this@EasyGateService, item.price)
        }
    }

    override val name: String = "easygate"
    override val displayName: String = "EasyGate"
    override val logoFile: File = ZxingUtil.easyGateLogo

    override fun queryOrder(order: Order): OrderStatus {
        return runBlocking {
            val response = fuel.get(
                request = Request.Builder()
                    .url("$baseUrl/order/query")
                    .headers(
                        mapOf(
                            "Client-ID" to clientId,
                            "Client-Secret" to clientSecret
                        )
                    )
                    .parameters(
                        listOf(
                            "orderId" to order.orderId
                        )
                    )
                    .build()
            )
            if (response.statusCode != 200) return@runBlocking OrderStatus.UNKNOWN
            val bodyText = response.body
            val jsonObject = JsonParser().parse(bodyText).asJsonObject
            val orderStatus = jsonObject["order_status"].asString
            return@runBlocking when (orderStatus) {
                "COMPLETED" -> OrderStatus.SUCCESS

                "WAITING_COMPLETE" -> OrderStatus.WAIT_PAY

                "WAITING_LOGIN" -> OrderStatus.WAIT_SCAN

                "NOT_EXIST_OR_TIMEOUT" -> OrderStatus.UNKNOWN

                else -> OrderStatus.UNKNOWN
            }
        }
    }

    override fun isInteractive(): Boolean {
        return true
    }
}