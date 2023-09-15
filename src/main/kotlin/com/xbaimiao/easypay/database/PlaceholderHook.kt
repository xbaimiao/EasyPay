package com.xbaimiao.easypay.database

import com.google.common.cache.CacheBuilder
import com.xbaimiao.easylib.bridge.PlaceholderExpansion
import com.xbaimiao.easylib.util.plugin
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * PlaceholderHook
 *
 * @author xbaimiao
 * @since 2023/9/14 17:19
 */
object PlaceholderHook : PlaceholderExpansion() {

    override val identifier: String = "easypay"
    override val version: String get() = plugin.description.version

    private val topNameCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<Int, String>()

    private val topPriceCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<Int, String>()

    /**
     * %easypay_count% 订单数量
     * %easypay_count_{player}% 指定玩家订单数量
     * %easypay_count_{player}_{service}% 指定玩家指定服务订单数量
     * %easypay_price_{player}% 指定玩家订单数量金额
     * %easypay_price_{player}_{service}% 指定玩家指定服务订单数量金额
     * %easypay_top_name_{1-10}% 排行榜用户名
     * %easypay_top_price_{1-10}% 排行榜金额
     */
    override fun onUUIDRequest(uuid: UUID, params: String): String? {
        if (!params.contains("_")) return "error"
        val paramsArgs = params.split("_")
        val type = paramsArgs[0]
        return when (type.lowercase()) {
            "count" -> {
                when (paramsArgs.size) {
                    1 -> Database.inst().getAllOrder().size.toString()
                    2 -> {
                        val player = paramsArgs[1]
                        Database.inst().getAllOrder(player).size.toString()
                    }

                    3 -> {
                        val player = paramsArgs[1]
                        val service = paramsArgs[2]
                        Database.inst().getAllOrder(player).filter { it.service == service }.size.toString()
                    }

                    else -> "error"
                }
            }

            "price" -> {
                when (paramsArgs.size) {
                    2 -> {
                        val player = paramsArgs[1]
                        Database.inst().getAllOrder(player).sumOf { it.item.price }.toString()
                    }

                    3 -> {
                        val player = paramsArgs[1]
                        val service = paramsArgs[2]
                        Database.inst().getAllOrder(player).filter { it.service == service }.sumOf { it.item.price }
                            .toString()
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
                                .groupBy { it.player!! }
                                // 计算每个player的订单总金额
                                .map { entry ->
                                    entry.key to entry.value.sumOf { it.item.price }
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