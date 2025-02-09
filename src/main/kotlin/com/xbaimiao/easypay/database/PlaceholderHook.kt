package com.xbaimiao.easypay.database

import com.google.common.cache.CacheBuilder
import com.xbaimiao.easylib.bridge.PlaceholderExpansion
import com.xbaimiao.easylib.util.EPlaceholderExpansion
import com.xbaimiao.easylib.util.plugin
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * PlaceholderHook
 *
 * @author xbaimiao
 * @since 2023/9/14 17:19
 */
@EPlaceholderExpansion
object PlaceholderHook : PlaceholderExpansion() {

    override val identifier: String = "easypay"
    override val version: String get() = plugin.description.version

    private val allCacheKey = UUID.randomUUID().toString()

    private val topNameCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<Int, String>()

    private val topPriceCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<Int, String>()

    private val priceCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<String, String>()

    private val countCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<String, String>()

    private fun String.trySelf(uuid: UUID): String {
        if (this.equals("self", true)) {
            return Bukkit.getPlayer(uuid)?.name ?: this
        }
        return this
    }

    /**
     * %easypay_count% 订单数量
     * %easypay_count:{player}% 指定玩家订单数量
     * %easypay_count:{player}:{service}% 指定玩家指定服务订单数量
     * %easypay_price:{player}% 指定玩家订单数量金额
     * %easypay_price:{player}:{service}% 指定玩家指定服务订单数量金额
     * %easypay_top:name:{1-10}% 排行榜用户名
     * %easypay_top:price:{1-10}% 排行榜金额
     */
    override fun onUUIDRequest(uuid: UUID, params: String): String {
        val paramsArgs = params.split(":")
        val type = paramsArgs[0]
        return when (type.lowercase()) {
            "count" -> {
                when (paramsArgs.size) {
                    1 -> {
                        countCache.getIfPresent(allCacheKey)?.let { return it }
                        Database.inst().getAllOrder().size.toString().also { countCache.put(allCacheKey, it) }
                    }

                    2 -> {
                        val player = paramsArgs[1].trySelf(uuid)
                        countCache.getIfPresent(player)?.let { return it }
                        Database.inst().getAllOrder(player).size.toString().also { countCache.put(player, it) }
                    }

                    3 -> {
                        val player = paramsArgs[1].trySelf(uuid)
                        val service = paramsArgs[2]
                        countCache.getIfPresent("$player-$service")?.let { return it }
                        Database.inst().getAllOrder(player).filter { it.service == service }.size.toString().also {
                            countCache.put("$player-$service", it)
                        }
                    }

                    else -> "error"
                }
            }

            "price" -> {
                when (paramsArgs.size) {
                    2 -> {
                        val player = paramsArgs[1].trySelf(uuid)
                        priceCache.getIfPresent(player)?.let { return it }
                        Database.inst().getAllOrder(player)
                            .sumOf { it.price }.toString()
                            .also { priceCache.put(player, it) }
                    }

                    3 -> {
                        val player = paramsArgs[1].trySelf(uuid)
                        val service = paramsArgs[2]
                        priceCache.getIfPresent("$player-$service")?.let { return it }
                        Database.inst().getAllOrder(player)
                            .filter { it.service == service }
                            .sumOf { it.price }
                            .toString().also {
                                priceCache.put("$player-$service", it)
                            }
                    }

                    else -> "error"
                }
            }

            "top" -> {
                when (paramsArgs.size) {
                    3 -> {
                        val index = paramsArgs[2].toInt()

                        fun getGroups(): Map<String, Double> {
                            val orders = Database.inst().getAllOrder()
                            if (orders.isEmpty()) return emptyMap()
                            return orders
                                // 通过player分组
                                .groupBy { it.playName }
                                // 计算每个player的订单总金额
                                .map { entry ->
                                    entry.key to entry.value.sumOf { it.price }
                                }
                                // 通过金额排序
                                .sortedByDescending { it.second }
                                // 转换为map
                                .toMap()
                        }

                        when (paramsArgs[1].lowercase()) {
                            "name" -> {
                                topNameCache.getIfPresent(index)?.let { return it }

                                val orders = Database.inst().getAllOrder()
                                if (orders.isEmpty()) return "null"

                                val groups = getGroups()

                                val top = groups.keys.toList().getOrNull(index - 1)
                                (top ?: "null").also { topNameCache.put(index, it) }
                            }

                            "price" -> {
                                topPriceCache.getIfPresent(index)?.let { return it }

                                val orders = Database.inst().getAllOrder()
                                if (orders.isEmpty()) return "null"

                                val groups = getGroups()
                                val top = groups.values.toList().getOrNull(index - 1)
                                (top?.toString() ?: "null").also { topPriceCache.put(index, it) }
                            }

                            else -> "error"
                        }
                    }

                    else -> "error"
                }
            }

            else -> "not found $type"
        }
    }

    fun init() {
        this.register()
    }

}
