package com.xbaimiao.easypay.service

import com.paypal.core.PayPalEnvironment
import com.paypal.core.PayPalHttpClient
import com.paypal.http.exceptions.HttpException
import com.paypal.orders.*
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import java.io.File

class PayPalService(
    environment: String,
    clientId: String,
    clientSecret: String,
    private val currency: String
) : DefaultPayService {
    private val paypalEnvironment: PayPalEnvironment = let {
        if (environment.equals("SANDBOX", ignoreCase = true)) {
            PayPalEnvironment.Sandbox(clientId, clientSecret)
        } else {
            PayPalEnvironment.Live(clientId, clientSecret)
        }
    }
    private var client: PayPalHttpClient = PayPalHttpClient(paypalEnvironment)
    override fun createOrder(player: String, item: Item): Order? {
        val orderRequest = OrderRequest()
        orderRequest.checkoutPaymentIntent("CAPTURE")

        val purchaseUnits: MutableList<PurchaseUnitRequest> = mutableListOf()
        purchaseUnits
            .add(
                PurchaseUnitRequest()
                    .amountWithBreakdown(
                        AmountWithBreakdown()
                            .currencyCode(currency)
                            .value(item.price.toString())
                    )
            )
        orderRequest.purchaseUnits(purchaseUnits)
        val request = OrdersCreateRequest().requestBody(orderRequest)
        val response = client.execute(request) ?: return null
        val approvalUrl: String = response.result().links().stream()
            .filter { link -> "approve" == link.rel() }
            .findFirst()
            .map(LinkDescription::href)
            .orElse(null)
        val tradeNo = response.result().id()
        val order = Order(tradeNo, item, approvalUrl, this, item.price)
        return order
    }

    override val name: String = "paypal"

    override val displayName: String = "PayPal"

    // 虽然没用但是还是加上了
    override val logoFile: File
        get() = ZxingUtil.paypalLogo

    override fun queryOrder(order: Order): OrderStatus {
        val orderId: String = order.orderId
        try {
            val request = OrdersGetRequest(orderId)
            val paypalOrder = client.execute(request).result()
            if (paypalOrder.status().equals("APPROVED", ignoreCase = true)) {
                val captureRequest = OrdersCaptureRequest(orderId)
                client.execute(captureRequest).result()
            } else if (paypalOrder.status().equals("COMPLETED", ignoreCase = true)) {
                return OrderStatus.SUCCESS
            }
        } catch (e: HttpException) {
            return OrderStatus.WAIT_SCAN
        }
        return OrderStatus.WAIT_PAY
    }

    override fun isInteractive(): Boolean {
        return true
    }

}