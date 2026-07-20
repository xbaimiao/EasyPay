package com.xbaimiao.easypay

import com.xbaimiao.baipay.sdk.BaiPayClient
import com.xbaimiao.baipay.sdk.model.PaymentChannel
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.database.MysqlHikariDatabase
import com.xbaimiao.easylib.database.SQLiteHikariDatabase
import com.xbaimiao.easylib.loader.DependencyLoader
import com.xbaimiao.easylib.loader.Loader
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.api.ItemProvider
import com.xbaimiao.easypay.api.ItemSack
import com.xbaimiao.easypay.database.Database
import com.xbaimiao.easypay.database.DefaultDatabase
import com.xbaimiao.easypay.entity.PayServiceProvider
import com.xbaimiao.easypay.item.CommandItem
import com.xbaimiao.easypay.item.CustomConfiguration
import com.xbaimiao.easypay.item.CustomPriceItemConfig
import com.xbaimiao.easypay.map.MapUtilProvider
import com.xbaimiao.easypay.map.VirtualMap
import com.xbaimiao.easypay.reward.RewardHandle
import com.xbaimiao.easypay.service.*
import com.xbaimiao.easypay.util.ConfigurationPatcher
import com.xbaimiao.ktor.KtorPluginsBukkit
import com.xbaimiao.ktor.KtorStat
import org.bukkit.Bukkit

@Suppress("unused")
class EasyPay : EasyPlugin(), KtorStat {

    override fun load() {
        // gson 需要优先加载
        val repoUrl = "https://maven.aliyun.com/repository/public/"
        val library = "com.google.code.gson:gson:2.11.0"
        val url = Loader.dependencyToUrl(library, repoUrl)
        val dependency = Loader.toDependenency(url, repoUrl, HashMap())
        DependencyLoader.load(this, dependency)

    }

    override fun enable() {
        launchCoroutine {
            // 初始化统计
            KtorPluginsBukkit.init(this@EasyPay, this@EasyPay)
            // userId 是用户Id 如果获取的时候报错 代表没有注入用户ID
            val userId = runCatching { userId }.getOrNull()
            if (userId != null) {
                info("$userId 感谢您的支持!")
            }

            saveDefaultConfig()

            ConfigurationPatcher.patchConfiguration("config.yml", "config.yml")
            ConfigurationPatcher.patchConfiguration("lang.yml", "lang.yml")

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
        VirtualMap.clearAllMaps()

        Bukkit.getScheduler().cancelTasks(this) // Cancel all running task - prevent throw exception while server close
    }

    fun loadCustomConfig() {
        val section = config.getConfigurationSection("builtin.CustomPriceItem")!!
        val customPriceItemConfig = CustomPriceItemConfig(
            section.getInt("min"),
            section.getInt("max"),
            section.getInt("ratio"),
            section.getStringList("commands"),
            section.getString("name")!!
        )
        ItemProvider.registerCustomItem(customPriceItemConfig)
        CustomConfiguration.setCustomPriceItemConfig(customPriceItemConfig)
    }

    fun loadMap() {
        val cancelOnDrop = config.getBoolean("map.cancel-on-drop")
        val mainHand = config.getString("map.hand") == "MAIN"
        VirtualMap.configure(mainHand, cancelOnDrop)
        MapUtilProvider.setMapUtil(VirtualMap)
    }

    fun loadDatabase() {
        val hikariDatabase = if (config.getBoolean("database.mysql.enable")) {
            MysqlHikariDatabase(config.getConfigurationSection("database.mysql")!!)
        } else {
            SQLiteHikariDatabase("database.db")
        }
        Database.setInst(DefaultDatabase(hikariDatabase))
        info("数据库加载成功 使用${hikariDatabase.javaClass.simpleName} 进行数据库操作")
        submit(async = true) {
            Database.inst().updateAllWebOrderTimeout()
        }
    }

    fun loadServices() {
        PayServiceProvider.clear()
        if (!config.getBoolean("alipay.enable")) {
            warn("支付宝官方支付未启用 跳过加载")
        } else {
            PayServiceProvider.registerService(
                AlipayService(
                    config.getString("alipay.appid")!!,
                    config.getString("alipay.private-key")!!,
                    config.getString("alipay.public-key")!!,
                    config.getString("alipay.api")!!,
                    config.getString("alipay.notify-url")!!,
                    config.getString("alipay.store-id")!!
                )
            )
        }

        if (!config.getBoolean("wechat-official.enable")) {
            warn("微信官方支付未启用 跳过加载")
        } else {
            PayServiceProvider.registerService(
                OfficialWeChatService(config.getConfigurationSection("wechat-official")!!)
            )
        }

        if (config.getBoolean("baipay.enable")) {
            val baseUrl = config.getString("baipay.base-url").orEmpty().trim()
            val appId = config.getString("baipay.app-id").orEmpty().trim()
            val keyId = config.getString("baipay.key-id").orEmpty().trim()
            val apiSecret = config.getString("baipay.api-secret").orEmpty().trim()
            if (baseUrl.isEmpty() || appId.isEmpty() || keyId.isEmpty() || apiSecret.isEmpty()) {
                warn("BaiPay配置不完整 跳过加载")
            } else {
                val client = BaiPayClient(baseUrl, appId, keyId, apiSecret)
                val returnUrl = config.getString("baipay.return-url").orEmpty().trim().ifEmpty { null }
                val waitTime = config.getInt("baipay.wait-time", 900).coerceAtLeast(1)
                var registered = false
                if (config.getBoolean("baipay.channels.wechat", true)) {
                    PayServiceProvider.registerService(
                        BaiPayService(client, PaymentChannel.WECHAT, returnUrl, waitTime)
                    )
                    registered = true
                }
                if (config.getBoolean("baipay.channels.alipay", true)) {
                    PayServiceProvider.registerService(
                        BaiPayService(client, PaymentChannel.ALIPAY, returnUrl, waitTime)
                    )
                    registered = true
                }
                if (!registered) {
                    warn("BaiPay未启用任何支付渠道 跳过加载")
                }
            }
        }
        PayServiceProvider.registerService(DevService)
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

                    "ItemSack" -> {
                        val itemList = section.getStringList("$name.items")
                        val items = mutableListOf<Item>()
                        itemList.forEach {
                            val item = ItemProvider.getItem(it)
                            if (item == null) {
                                warn("| 注册商品包时无法找到商品: $it 请检查商品是否存在或调整配置顺序")
                            } else {
                                items.add(item)
                            }
                        }
                        ItemProvider.register(ItemSack(items))
                    }

                    else -> warn("未知商品类型 $type")
                }
            }
        }
    }

}
