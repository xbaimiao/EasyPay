package com.xbaimiao.easypay.service

import com.stripe.Stripe
import com.stripe.model.Price
import com.stripe.model.checkout.Session
import com.stripe.param.PriceCreateParams
import com.stripe.param.checkout.SessionCreateParams
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import java.io.File
import kotlin.math.roundToLong

class StripeService(
    apiKey: String,
    private val currency: String,
    private val successUrl: String
) : DefaultPayService {

    init {
        Stripe.apiKey = apiKey
    }

    override fun createOrder(player: String, item: Item): Order? {
        try {
            val priceCreateParams = PriceCreateParams.builder()
                .setCurrency(currency.lowercase())
                .setUnitAmount((item.price * 100).roundToLong())
                .setProductData(
                    PriceCreateParams.ProductData.builder()
                        .setName(item.name)
                        .build()
                )
                .build()
            val price = Price.create(priceCreateParams)
            val sessionCreateParams = SessionCreateParams.builder()
                .setSuccessUrl(successUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(price.id)
                        .setQuantity(1L)
                        .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .build()
            val session = Session.create(sessionCreateParams)
            val tradeNo = session.id
            val order = Order(tradeNo, item, session.url, this, item.price)
            return order
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            return null
        }
    }

    override val name: String = "stripe"
    override val displayName: String = "Stripe"

    // 也是没用 但还是加了
    override val logoFile: File = ZxingUtil.stripeLogo

    override fun queryOrder(order: Order): OrderStatus {
        val session = Session.retrieve(order.orderId)
        when (session.status.lowercase()) {
            "complete" -> {
                if (session.paymentStatus.lowercase() == "paid") {
                    return OrderStatus.SUCCESS
                }
                return OrderStatus.WAIT_PAY
            }

            "open" -> {
                return OrderStatus.WAIT_PAY
            }
        }
        return OrderStatus.WAIT_SCAN
    }

    override fun isInteractive(): Boolean {
        return true
    }
}