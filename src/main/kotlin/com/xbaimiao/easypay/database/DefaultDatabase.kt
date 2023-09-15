package com.xbaimiao.easypay.database

import com.google.common.cache.CacheBuilder
import com.xbaimiao.easylib.database.SQLDatabase
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
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
    private val allCacheKey = UUID.randomUUID().toString()

    // 一个用于缓存所有订单的缓存
    private val orderCache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(3, TimeUnit.SECONDS)
        .build<String, Collection<Order>>()

    init {
        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement(
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
            statement.use { it.executeUpdate() }
        }
    }

    private fun ResultSet.toOrder(): Order {
        val orderId = getString("order_id")
        val itemEntity: Item
        ObjectInputStream(ByteArrayInputStream(getBytes("item_entity"))).use {
            itemEntity = it.readObject() as Item
        }
        return Order(orderId, itemEntity, getString("qr_code"), getString("service")).also {
            it.player = getString("player")
        }
    }

    override fun getAllOrder(): Collection<Order> {
        orderCache.getIfPresent(allCacheKey)?.let { return it }

        return sqlDatabase.useConnection { connection ->
            val allOrder = ArrayList<Order>()

            val statement = connection.prepareStatement("SELECT * FROM $table;")
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    allOrder.add(resultSet.toOrder())
                }
            }
            statement.close()

            allOrder.also { orderCache.put(allCacheKey, it) }
        }
    }

    override fun getAllOrder(playerName: String): Collection<Order> {
        orderCache.getIfPresent(playerName)?.let { return it }

        return sqlDatabase.useConnection { connection ->
            val allOrder = ArrayList<Order>()

            val statement = connection.prepareStatement("SELECT * FROM $table WHERE player=?;")
            statement.setString(1, playerName)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    allOrder.add(resultSet.toOrder())
                }
            }
            statement.close()

            allOrder.also { orderCache.put(playerName, it) }
        }
    }

    override fun addOrder(playerName: String, order: Order) {
        val out = ByteArrayOutputStream()
        ObjectOutputStream(out).use {
            it.writeObject(order.item)
        }

        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement("INSERT INTO `$table` VALUES (?,?,?,?,?,?,?);")
            statement.setString(1, playerName)
            statement.setString(2, order.orderId)
            statement.setString(3, order.item.name)
            statement.setBytes(4, out.toByteArray())
            statement.setDouble(5, order.item.price)
            statement.setString(6, order.qrCode)
            statement.setString(7, order.service)
            statement.use { it.executeUpdate() }
        }
    }

}