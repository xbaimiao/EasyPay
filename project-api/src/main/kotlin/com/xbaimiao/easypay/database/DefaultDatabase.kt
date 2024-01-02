package com.xbaimiao.easypay.database

import com.xbaimiao.easylib.database.SQLDatabase
import java.sql.ResultSet

/**
 * DefaultDatabase
 *
 * @author xbaimiao
 * @since 2023/9/13 23:24
 */
class DefaultDatabase(private val sqlDatabase: SQLDatabase) : Database {

    private val table = "easy_pay_order"
    private val rewardTable = "easy_pay_reward"
    private val webOrderTable = "easy_pay_web_order"

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

            val createWebOrderTable = connection.prepareStatement(
                """CREATE TABLE IF NOT EXISTS `$webOrderTable`(
                    |`order_id` VARCHAR(64) PRIMARY KEY ,
                    |`create_time` LONG,
                    |`pay_time` LONG,
                    |`send_time` LONG,
                    |`desc` VARCHAR(255),
                    |`pay_type` VARCHAR(10),
                    |`price` DOUBLE(100,2),
                    |`player` VARCHAR(32),
                    |`status` VARCHAR(16),
                    |`send_log` VARCHAR(255)
                    |);""".trimMargin()
            )
            createWebOrderTable.use { it.executeUpdate() }
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

        return sqlDatabase.useConnection { connection ->
            val allOrder = ArrayList<OrderData>()

            val statement = connection.prepareStatement("SELECT * FROM $table;")
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    allOrder.add(resultSet.toOrderData())
                }
            }
            statement.close()

            allOrder
        }
    }

    override fun getAllOrder(playerName: String): Collection<OrderData> {

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

            allOrder
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
        addOrder(
            playerName,
            OrderData(System.currentTimeMillis().toString(), "手动修改累充金额", "null", "null", num, playerName)
        )
    }

    @Synchronized
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

    @Synchronized
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

    override fun addWebOrder(webOrder: WebOrder) {
        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement("INSERT INTO `$webOrderTable` VALUES (?,?,?,?,?,?,?,?,?,?);")
            statement.setString(1, webOrder.orderId)
            statement.setLong(2, webOrder.createTime)
            statement.setLong(3, webOrder.payTime)
            statement.setLong(4, webOrder.sendTime)
            statement.setString(5, webOrder.desc)
            statement.setString(6, webOrder.payType)
            statement.setDouble(7, webOrder.price)
            statement.setString(8, webOrder.player)
            statement.setString(9, webOrder.status.name)
            statement.setString(10, webOrder.sendLog)
            statement.use { it.executeUpdate() }
        }
    }

    override fun getWebOrder(orderId: String): WebOrder? {
        return sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement("SELECT * FROM `$webOrderTable` WHERE `order_id` = ?;")
            statement.setString(1, orderId)
            var webOrder: WebOrder? = null
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    webOrder = WebOrder(
                        resultSet.getLong("create_time"),
                        resultSet.getLong("pay_time"),
                        resultSet.getLong("send_time"),
                        resultSet.getString("desc"),
                        resultSet.getString("order_id"),
                        resultSet.getString("pay_type"),
                        resultSet.getDouble("price"),
                        resultSet.getString("player"),
                        WebOrder.Status.valueOf(resultSet.getString("status")),
                        resultSet.getString("send_log"),
                    )
                }
            }
            statement.close()
            webOrder
        }
    }

    override fun updateWebOrder(webOrder: WebOrder) {
        sqlDatabase.useConnection { connection ->
            val statement = connection.prepareStatement(
                """UPDATE `$webOrderTable` 
                SET `pay_time` = ?, `send_time` = ?, `status` = ?, `send_log` = ?
                WHERE `order_id` = ?;""".trimMargin()
            )
            statement.setLong(1, webOrder.payTime)
            statement.setLong(2, webOrder.sendTime)
            statement.setString(3, webOrder.status.name)
            statement.setString(4, webOrder.sendLog)
            statement.setString(5, webOrder.orderId)
            statement.use { it.executeUpdate() }
        }
    }

}