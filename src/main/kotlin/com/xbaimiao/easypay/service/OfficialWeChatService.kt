package com.xbaimiao.easypay.service

import com.wechat.pay.java.core.RSAAutoCertificateConfig
import com.wechat.pay.java.core.RSAConfig
import com.wechat.pay.java.core.RSAPublicKeyConfig
import com.wechat.pay.java.service.payments.model.Transaction
import com.wechat.pay.java.service.payments.nativepay.NativePayService
import com.wechat.pay.java.service.payments.nativepay.model.Amount
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.OrderStatus
import com.xbaimiao.easypay.util.ZxingUtil
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
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
    merchantSerialNumber: String,
    privateKeyPath: String,
    wechatPayCertificatePath: String?,
    apiV3Key: String?,
    publicKeyPath: String?,
    publicKeyId: String?
) : DefaultPayService {

    constructor(config: ConfigurationSection) : this(
        config.getString("mchid")!!,
        config.getString("appid")!!,
        config.getString("merchantSerialNumber")!!,
        plugin.dataFolder.path + File.separator + config.getString("privateKeyPath")!!,
        config.getString("wechatPayCertificatePath")?.let { plugin.dataFolder.path + File.separator + it },
        //since 1.3.4
        config.getString("apiV3Key"),
        config.getString("publicKeyPath")?.let { plugin.dataFolder.path + File.separator + it },
        config.getString("publicKeyId")
    )

    private val service: NativePayService by lazy {
        val config = when {
            //如果存在 微信支付平台证书 兼容老版本
            wechatPayCertificatePath != null -> {
                RSAConfig.Builder().merchantId(mchid)
                    .privateKeyFromPath(privateKeyPath)
                    .merchantSerialNumber(merchantSerialNumber)
                    .wechatPayCertificatesFromPath(wechatPayCertificatePath)
                    .build()
            }

            //如果存在 apiV3Key 和公钥地址 使用新版
            apiV3Key != null && publicKeyPath != null -> {
                RSAPublicKeyConfig.Builder()
                    .merchantId(mchid)
                    .privateKeyFromPath(privateKeyPath)
                    .merchantSerialNumber(merchantSerialNumber)
                    .apiV3Key(apiV3Key)
                    .publicKeyFromPath(publicKeyPath)
                    .publicKeyId(publicKeyId)
                    .build()
            }
            //此情况适合有 apiV3Key 但是没有申请过 微信支付平台证书 这样做无需手动申请微信支付平台证书了
            apiV3Key != null -> {
                RSAAutoCertificateConfig.Builder()
                    .merchantId(mchid)
                    .privateKeyFromPath(privateKeyPath)
                    .merchantSerialNumber(merchantSerialNumber)
                    .apiV3Key(apiV3Key)
                    .build()
            }

            else -> throw IllegalArgumentException("Invalid configuration: either wechatPayCertificatePath or apiV3Key must be provided")
        }

        NativePayService.Builder().config(config).build()
    }

    override val name: String = "wechat-official"

    override val displayName: String = "微信官方"
    override val logoFile: File
        get() = ZxingUtil.wechatLogo

    override fun createOrder(player: String, item: Item): Order? {
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
        val offlinePlayer = Bukkit.getOfflinePlayer(player)

        if (offlinePlayer.isOnline) {
            debug("offline player online execute preCreate")
            if (!item.preCreate(offlinePlayer.player!!, this, order)) {
                return null
            }
        }

        return order
    }

    override fun isInteractive(): Boolean {
        return false
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
