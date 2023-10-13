package com.xbaimiao.easypay.service

import com.wechat.pay.java.core.RSAConfig
import com.wechat.pay.java.service.payments.model.Transaction
import com.wechat.pay.java.service.payments.nativepay.NativePayService
import com.wechat.pay.java.service.payments.nativepay.model.Amount
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File

/**
 * OfficialWeChatService
 *
 * @author xbaimiao
 * @since 2023/10/13 23:34
 */
class OfficialWeChatService(
    private val mchid: String,
    private val appId: String,
    privateKeyPath: String,
    wechatPayCertificatePath: String,
    merchantSerialNumber: String
) : DefaultPayService {

    constructor(config: ConfigurationSection) : this(
        config.getString("mchid")!!,
        config.getString("appid")!!,
        plugin.dataFolder.path + File.separator + config.getString("privateKeyPath")!!,
        plugin.dataFolder.path + File.separator + config.getString("wechatPayCertificatePath")!!,
        config.getString("merchantSerialNumber")!!
    )

    private val service: NativePayService by lazy {
        // 初始化商户配置
        val config = RSAConfig.Builder().merchantId(mchid).privateKeyFromPath(privateKeyPath)
            .merchantSerialNumber(merchantSerialNumber).wechatPayCertificatesFromPath(wechatPayCertificatePath).build()

        // 初始化服务
        NativePayService.Builder().config(config).build()
    }

    override val name: String = "wechat-official"

    override fun createOrder(player: Player, item: Item): Order? {
        val tradeNo = generateOrderId()
        val request = PrepayRequest()
        request.appid = appId
        request.mchid = mchid
        request.description = item.name
        val money = Amount()
        money.currency = "CNY"
        money.total = (item.price * 100.0).toInt()
        request.amount = money
        request.outTradeNo = tradeNo
        request.notifyUrl = "https://www.baidu.com"
        val result = service.prepay(request)

        val order = Order(tradeNo, item, result.codeUrl, this, item.price)
        if (!item.preCreate(player, this, order)) {
            return null
        }

        return order
    }

    override fun queryOrder(order: Order): OrderStatus {
        val request = QueryOrderByOutTradeNoRequest()
        request.outTradeNo = order.orderId
        request.mchid = mchid
        val response = service.queryOrderByOutTradeNo(request)
        return when (response.tradeState) {
            Transaction.TradeStateEnum.SUCCESS -> OrderStatus.SUCCESS
            Transaction.TradeStateEnum.USERPAYING -> OrderStatus.WAIT_PAY
            else -> OrderStatus.UNKNOWN
        }
    }

}