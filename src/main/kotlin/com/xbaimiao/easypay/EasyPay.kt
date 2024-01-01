package com.xbaimiao.easypay

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.database.MysqlHikariDatabase
import com.xbaimiao.easylib.database.SQLiteHikariDatabase
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.DefaultDatabase
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.item.CommandItem
import com.xbaimiao.easypay.item.CustomConfiguration
import com.xbaimiao.easypay.item.CustomPriceItemConfig
import com.xbaimiao.easypay.map.MapUtilProvider
import com.xbaimiao.easypay.map.RealMap
import com.xbaimiao.easypay.reward.RewardHandle
import com.xbaimiao.easypay.service.AlipayService
import com.xbaimiao.easypay.service.DLCWeChatService
import com.xbaimiao.easypay.service.OfficialWeChatService
import com.xbaimiao.ktor.KtorPluginsBukkit
import com.xbaimiao.ktor.KtorStat

@Suppress("unused")
class EasyPay : EasyPlugin(), KtorStat {

    override fun enable() {
        launchCoroutine {
            // 初始化统计
            KtorPluginsBukkit.init(this@EasyPay, this@EasyPay)
            // userId 是用户Id 如果获取的时候报错 代表没有注入用户ID
            val userId = runCatching { userId }.getOrNull()
            if (userId != null) {
                info("$userId 感谢您的支持!")
                val has = async {
                    hasPlugin("EasyPay")
                }
                if (!has) {
                    error("$userId 未从您的购买列表 未找到EasyPay插件 无法使用")
                }
                // 统计服务器在线的方法
                stat()
            }
            saveDefaultConfig()

            loadCustomConfig()
            loadMap()
            loadServices()
            loadItems()
            loadDatabase()
            RewardHandle.loadConfiguration()

            rootCommand.register()
        }
    }

    override fun disable() {
        val dlcWeChatService = PayServiceProvider.getService(DLCWeChatService::class.java)
        if (dlcWeChatService != null) {
            info("正在断开与WalletMonitor的连接")
            for (orderPrice in DLCWeChatService.list) {
                dlcWeChatService.walletConnector.orderTimeout(orderPrice)
            }
            dlcWeChatService.walletConnector.close()
        }
    }

    fun loadCustomConfig() {
        val section = config.getConfigurationSection("builtin.CustomPriceItem")
        val customPriceItemConfig = CustomPriceItemConfig(
            section.getInt("min"),
            section.getInt("max"),
            section.getInt("ratio"),
            section.getStringList("commands"),
            section.getString("name")
        )
        CustomConfiguration.setCustomPriceItemConfig(customPriceItemConfig)
    }

    fun loadMap() {
        val cancelOnDrop = config.getBoolean("map.cancel-on-drop")
        MapUtilProvider.setMapUtil(RealMap(config.getString("map.hand") == "MAIN", cancelOnDrop))
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
            warn("未配置支付宝Service 跳过加载")
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
            warn("未配置微信支付服务(DLC,监听消息) 跳过加载内容")
        } else {
            PayServiceProvider.registerService(
                DLCWeChatService(
                    config.getString("wechat.server"), config.getString("wechat.qrcode")
                )
            )
        }

        if (config.getString("wechat-official.appid") == "wx5exxxxxxxxx") {
            warn("未配置微信支付官方Service 跳过加载")
        } else {
            PayServiceProvider.registerService(
                OfficialWeChatService(config.getConfigurationSection("wechat-official"))
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