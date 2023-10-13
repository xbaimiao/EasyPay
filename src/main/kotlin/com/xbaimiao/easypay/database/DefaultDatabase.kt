package com.xbaimiao.easypay.database

import com.google.common.cache.CacheBuilder
import com.xbaimiao.easylib.database.SQLDatabase
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * DefaultDatabase
 *
 * @author xbaimiao
 * @since 2023/9/13 23:24
 */
class DefaultDatabase(private val sqlDatabase: SQLDatabase) : Database {

    private val table = "easy_pay_order"
    private val rewardTable = "easy_pay_reward"
    private val allCacheKey = UUID.randomUUID().toString()

    // 一个用于缓存所有订单的缓存
    private val orderCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(3, TimeUnit.SECONDS)
        .build<String, Collection<OrderData>>()

    init {
        sqlDatabase.useConnection { connection ->
            val createOrderTable = connection.prepareStatement(
                """CREATE TABLE IF NOT EXISTS `$table`(
                |player VARCHAR(16),
                |order_id VARCHAR(64),
                |item_name VARCHAR(32),
                |item_entity LONGBLOB,
                |price DOUBLE(8,2),
                |qr_code VARCHAR(255),
                |service VARCHAR(32)
                |);""".trimMargin()
            )
            createOrderTable.use { it.executeUpdate() }

            val createRewardTable = connection.prepareStatement(
                """CREATE TABLE IF NOT EXISTS `$rewardTable`(
                |player VARCHAR(16),
                |reward VARCHAR(64)
                |);""".trimMargin()
            )
            createRewardTable.use { it.executeUpdate() }
        }
    }

    private fun ResultSet.toOrderData(): OrderData {
        val orderId = getString("order_id")
        val item = runCatching { String(getBytes("item_entity")) }.getOrElse { "Legacy" }
        return OrderData(
            orderId,
            item,
            getString("qr_code"),
            getString("service"),
            getDouble("price"),
            getString("player")
        )
    }

    override fun getAllOrder(): Collection<OrderData> {
        orderCache.getIfPresent(allCacheKey)?.let { return it }

        return sqlDatabase.useConnection { connection ->
            val allOrder = ArrayList<OrderData>()

            val statement = connection.prepareStatement("SELECT * FROM $table;")
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    allOrder.add(resultSet.toOrderData())
                }
            }
            statement.close()

            allOrder.also { orderCache.put(allCacheKey, it) }
        }
    }

    override fun getAllOrder(playerName: String): Collection<OrderData> {
        orderCache.getIfPresent(playerName)?.let { return it }

        return sqlDatabase.useConnection { connection ->
            val allOrder = ArrayList<OrderData>()

            val statement = connection.prepareStatement("SELECT * FROM `$table` WHERE `player`=?;")
            statement.setString(1, playerName)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    allOrder.add(resultSet.toOrderData())
                }
            }
            statement.close()

            allOrder.also { orderCache.put(playerName, it) }
        }
    }

    override fun addOrder(playerName: String, order: OrderData) {
        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement("INSERT INTO `$table` VALUES (?,?,?,?,?,?,?);")
            statement.setString(1, playerName)
            statement.setString(2, order.orderId)
            statement.setString(3, order.item)
            statement.setBytes(4, order.item.toByteArray())
            statement.setDouble(5, order.price)
            statement.setString(6, order.qrCode)
            statement.setString(7, order.service)
            statement.use { it.executeUpdate() }
        }
    }

    override fun addRewardPrice(playerName: String, num: Double) {
        addOrder(playerName, OrderData(System.currentTimeMillis().toString(), "手动修改累充金额", "null", "null", num, playerName))
    }

    override fun canGetReward(playerName: String, reward: String): Boolean {
        return sqlDatabase.useConnection { connection ->
            val statement =
                connection.prepareStatement("SELECT * FROM `$rewardTable` WHERE `player` = ? AND `reward`=?;")
            statement.setString(1, playerName)
            statement.setString(2, reward)
            statement.use {
                val resultSet = it.executeQuery()
                !resultSet.next()
            }
        }
    }

    override fun setGetReward(playerName: String, reward: String) {
        if (!canGetReward(playerName, reward)) {
            error("it has been set to claim rewards")
        }
        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement("INSERT INTO `$rewardTable` VALUES (?,?);")
            statement.setString(1, playerName)
            statement.setString(2, reward)
            statement.use { it.executeUpdate() }
        }
    }

}