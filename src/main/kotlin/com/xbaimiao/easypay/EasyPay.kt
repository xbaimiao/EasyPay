package com.xbaimiao.easypay

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.database.MysqlHikariDatabase
import com.xbaimiao.easylib.database.SQLiteHikariDatabase
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
import com.xbaimiao.easypay.functions.*
import com.xbaimiao.easypay.item.CommandItem
import com.xbaimiao.easypay.item.CustomConfiguration
import com.xbaimiao.easypay.item.CustomPriceItemConfig
import com.xbaimiao.easypay.map.MapUtilProvider
import com.xbaimiao.easypay.map.RealMap
import com.xbaimiao.easypay.map.VirtualMap
import com.xbaimiao.easypay.reward.RewardHandle
import com.xbaimiao.easypay.service.AlipayService
import com.xbaimiao.easypay.service.DLCWeChatService
import com.xbaimiao.easypay.service.OfficialWeChatService
import com.xbaimiao.easypay.util.FunctionUtil
import com.xbaimiao.ktor.KtorPluginsBukkit
import com.xbaimiao.ktor.KtorStat
import org.bukkit.Bukkit

@Suppress("unused")
class EasyPay : EasyPlugin(), KtorStat {

    override fun load() {
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.disableUpdateCheck()
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.disableBStats()
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.disablePackageWarning()
    }

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

            val functionUtil = FunctionUtil()
            val functionManager = functionUtil.getFastExpression().functionManager
            functionManager.register(TitleFunction(), "标题")
            functionManager.register(MessageFunction(), "消息", "msg")

            when {
                config.getBoolean("no-evalex") -> {
                    info("已启用无EvalEx函数替代方案 将使用expression-evaluator替代EvalEx对if函数进行解析")
                    functionManager.register(ExpEvalConditionFunction(), "条件", "如果")
                }
                // EvalEx 兼容 - Java11往下使用EvalEx2进行条件解析, Java11+使用EvalEx3
                System.getProperty("java.version").split(".")[0].toInt() >= 11 -> {
                    info("已检测到插件在 Java11+ 环境中运行 条件函数将使用EvalEx3模式")
                    functionManager.register(ConditionFunction(), "条件", "如果")
                }

                else -> {
                    info("已检测到插件在 Java8-10 环境中运行 条件函数将使用EvalEx2模式 (与EvalEx3函数不兼容)")
                    functionManager.register(EvalEx2ConditionFunction(), "条件", "如果")
                }
            }

            functionManager.register(ReturnFunction(), "返回", "取消", "结束")
            functionManager.register(HasPermissionFunction(), "perm", "权限", "permission")
            functionManager.register(ExecuteFunction(), "执行命令", "执行", "cmd", "exec", "command")
            functionManager.register(
                PlayerExecuteFunction(), "玩家命令", "玩家执行", "cmdPlayer", "commandPlayer", "execPlayer", "player"
            )
            functionManager.register(CancelOrderFunction(), "取消订单", "取消", "c")
            functionManager.register(ChangePriceFunction(), "更改价格", "价格", "cost", "amount")

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
            section.getStringList("actions"),
            section.getStringList("pre-actions"),
            section.getStringList("rewards"),
            section.getStringList("commands"),
            section.getString("name")
        )
        ItemProvider.registerCustomItem(customPriceItemConfig)
        CustomConfiguration.setCustomPriceItemConfig(customPriceItemConfig)
    }

    fun loadMap() {
        val cancelOnDrop = config.getBoolean("map.cancel-on-drop")
        val virtualMode = config.getBoolean("map.virtual")
        val mainHand = config.getString("map.hand") == "MAIN"
        val mapUtil = if (virtualMode && checkProtocolLib()) {
            info("EasyPay正在使用发包地图模式")
            info("发包地图仅支持最新的Minecraft版本")
            info("如您在较旧的服务器版本上使用发包地图遇到问题 请关闭此功能 提出兼容请求将不会被处理")
            VirtualMap(mainHand, cancelOnDrop)
        } else RealMap(mainHand, cancelOnDrop)
        MapUtilProvider.setMapUtil(mapUtil)
    }

    private fun checkProtocolLib(): Boolean {
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            val version = Bukkit.getPluginManager()
                .getPlugin("ProtocolLib").description.version
                .split("-")[0].replace(".", "").toInt()
            return version > 510
        }
        return false
    }

    fun loadDatabase() {
        val hikariDatabase = if (config.getBoolean("database.mysql.enable")) {
            MysqlHikariDatabase(config.getConfigurationSection("database.mysql"))
        } else {
            SQLiteHikariDatabase("database.db")
        }
        Database.setInst(DefaultDatabase(hikariDatabase))
        submit(async = true) {
            Database.inst().updateAllWebOrderTimeout()
        }
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
                        val actions = section.getStringList("$name.actions")
                        val preActions = section.getStringList("$name.pre-actions")
                        val rewards = section.getStringList("$name.rewards")
                        ItemProvider.register(CommandItem(price, name, commands, actions, preActions, rewards))
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