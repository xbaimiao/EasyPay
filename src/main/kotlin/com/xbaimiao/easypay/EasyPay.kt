package com.xbaimiao.easypay

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.impl.AlipayService

@Suppress("unused")
class EasyPay : EasyPlugin() {

    override fun enable() {
        saveDefaultConfig()

        if (config.getString("alipay.appid") == "appid") {
            warn("未配置支付宝Service 不加载")
        } else {
            PayServiceProvider.registerService(
                AlipayService(
                    config.getString("alipay.appid"),
                    config.getString("alipay.private-key"),
                    config.getString("alipay.public-key"),
                    config.getString("alipay.api"),
                    config.getString("alipay.notify-url"),
                    config.getString("alipay.store-id")
                )
            )
        }

        rootCommand.register()
    }

}