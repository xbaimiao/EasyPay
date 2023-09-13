package com.xbaimiao.easypay

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.database.MysqlHikariDatabase
import com.xbaimiao.easylib.database.SQLiteHikariDatabase
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.CommandItem
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.DefaultDatabase
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.functions.TitleFunction
import com.xbaimiao.easypay.service.AlipayService
import com.xbaimiao.easypay.service.WeChatService

@Suppress("unused")
class EasyPay : EasyPlugin() {

    override fun enable() {
        saveDefaultConfig()

        loadServices()
        loadItems()
        loadDatabase()

        FunctionUtil.functionManager.functionManager.register(TitleFunction(), "标题")

        rootCommand.register()
    }

    fun loadDatabase() {
        val hikariDatabase = if (config.getBoolean("database.mysql.enable")) {
            MysqlHikariDatabase(config.getConfigurationSection("database.mysql"))
        } else {
            SQLiteHikariDatabase("database.db")
        }
        Database.setInst(DefaultDatabase(hikariDatabase))
    }

    fun loadServices() {
        PayServiceProvider.clear()
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

        if (!config.getBoolean("wechat.enable")) {
            warn("未配置微信支付服务(DLC) 跳过加载内容")
        } else {
            PayServiceProvider.registerService(
                WeChatService(
                    config.getString("wechat.server"),
                    config.getString("wechat.qrcode")
                )
            )
        }
    }

    fun loadItems() {
        ItemProvider.clear()
        config.getConfigurationSection("items")?.let { section ->
            for (name in section.getKeys(false)) {
                val type = section.getString("$name.type")
                val price = section.getDouble("$name.price")
                when (type) {
                    "CommandItem" -> {
                        val commands = section.getStringList("$name.commands")
                        ItemProvider.register(CommandItem(price, name, commands))
                    }

                    else -> warn("未知商品类型 $type")
                }
            }
        }
    }

}