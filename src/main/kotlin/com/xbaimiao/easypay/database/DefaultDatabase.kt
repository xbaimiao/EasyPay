package com.xbaimiao.easypay.database

import com.xbaimiao.easylib.database.SQLDatabase
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * DefaultDatabase
 *
 * @author xbaimiao
 * @since 2023/9/13 23:24
 */
class DefaultDatabase(private val sqlDatabase: SQLDatabase) : Database {

    private val table = "easy_pay_order"

    init {
        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement(
                """CREATE TABLE IF NOT EXISTS `$table`(
                |player VARCHAR(16),
                |order_id VARCHAR(64),
                |item_name VARCHAR(32),
                |item_entity LONGBLOB,
                |price DOUBLE(8,2),
                |qr_code VARCHAR(255)
                |);""".trimMargin()
            )
            statement.use { it.executeUpdate() }
        }
    }

    override fun getAllOrder(playerName: String): Collection<Order> {
        return sqlDatabase.useConnection { connection ->
            val allOrder = ArrayList<Order>()

            val statement = connection.prepareStatement("SELECT * FROM $table WHERE player=?;")
            statement.setString(1, playerName)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    val orderId = resultSet.getString("order_id")
                    val itemEntity: Item
                    ObjectInputStream(ByteArrayInputStream(resultSet.getBytes("item_entity"))).use {
                        itemEntity = it.readObject() as Item
                    }
                    allOrder.add(Order(orderId, itemEntity, resultSet.getString("qr_code")))
                }
            }
            statement.close()

            allOrder
        }
    }

    override fun addOrder(playerName: String, order: Order) {
        val out = ByteArrayOutputStream()
        ObjectOutputStream(out).use {
            it.writeObject(order.item)
        }

        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement("INSERT INTO `$table` VALUES (?,?,?,?,?,?);")
            statement.setString(1, playerName)
            statement.setString(2, order.orderId)
            statement.setString(3, order.item.name)
            statement.setBytes(4, out.toByteArray())
            statement.setDouble(5, order.item.price)
            statement.setString(6, order.qrCode)
            statement.use { it.executeUpdate() }
        }
    }

}