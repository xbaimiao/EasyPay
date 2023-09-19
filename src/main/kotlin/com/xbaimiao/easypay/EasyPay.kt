package com.xbaimiao.easypay

import com.github.retrooper.packetevents.PacketEvents
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.database.MysqlHikariDatabase
import com.xbaimiao.easylib.database.SQLiteHikariDatabase
import com.xbaimiao.easylib.nms.NMSMap
import com.xbaimiao.easylib.skedule.schedule
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.DefaultDatabase
import com.xbaimiao.easypay.database.PlaceholderHook
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.functions.*
import com.xbaimiao.easypay.item.CommandItem
import com.xbaimiao.easypay.map.MapUtilProvider
import com.xbaimiao.easypay.map.VirtualMap
import com.xbaimiao.easypay.service.AlipayService
import com.xbaimiao.easypay.service.WeChatService
import com.xbaimiao.ktor.KtorPluginsBukkit
import com.xbaimiao.ktor.KtorStat
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder

@Suppress("unused")
class EasyPay : EasyPlugin(), KtorStat {

    override fun load() {
        info("Loading PacketEvents...")

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.bStats(true).checkForUpdates(false).debug(false)
        PacketEvents.getAPI().load()
    }

    override fun enable() {
        PacketEvents.getAPI().init()
        schedule {
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
            }
            // 统计服务器在线的方法
            stat()
            saveDefaultConfig()

            loadMap()
            loadServices()
            loadItems()
            loadDatabase()

            val functionManager = FunctionUtil.functionManager.functionManager
            functionManager.register(TitleFunction(), "标题")
            functionManager.register(MessageFunction(), "消息", "msg")
            functionManager.register(ConditionFunction(), "条件", "如果")
            functionManager.register(ReturnFunction(), "返回", "取消", "结束")
            functionManager.register(HasPermissionFunction(), "perm", "权限", "permission")
            functionManager.register(ExecuteFunction(), "执行命令", "执行", "cmd", "exec", "command")
            functionManager.register(
                PlayerExecuteFunction(),
                "玩家命令",
                "玩家执行",
                "cmdPlayer",
                "commandPlayer",
                "execPlayer",
                "player"
            )
            functionManager.register(CancelOrderFunction(), "取消订单", "取消", "c")
            functionManager.register(ChangePriceFunction(), "更改价格", "价格", "cost", "amount")

            PlaceholderHook.init()
            rootCommand.register()
        }
    }

    override fun disable() {
        val weChatService = PayServiceProvider.getService(WeChatService::class.java)
        if (weChatService != null) {
            info("正在断开与WalletMonitor的连接")
            for (orderPrice in WeChatService.list) {
                weChatService.walletConnector.orderTimeout(orderPrice)
            }
            weChatService.walletConnector.close()
        }
        PacketEvents.getAPI().terminate()
    }

    fun loadMap() {
        val cancelOnDrop = config.getBoolean("map.cancel-on-drop")
        MapUtilProvider.setMapUtil(
            VirtualMap(
                NMSMap.Hand.valueOf(config.getString("map.hand").uppercase()),
                cancelOnDrop
            )
        )
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
                        val actions = section.getStringList("$name.actions")
                        val preActions = section.getStringList("$name.pre-actions")
                        val rewards = section.getStringList("$name.rewards")
                        ItemProvider.register(CommandItem(price, name, commands, actions, preActions, rewards))
                    }

                    else -> warn("未知商品类型 $type")
                }
            }
        }
    }

}