package com.xbaimiao.easypay.database

import com.xbaimiao.easypay.entity.Order

/**
 * Database
 *
 * @author xbaimiao
 * @since 2023/9/13 23:23
 */
interface Database {

    fun getAllOrder(): Collection<Order>

    /**
     * 获取玩家支付的所有订单
     */
    fun getAllOrder(playerName: String): Collection<Order>

    /**
     * 为玩家添加一个订单
     */
    fun addOrder(playerName: String, order: Order)

    companion object {

        private var INST: Database? = null

        fun inst(): Database {
            return INST!!
        }

        fun setInst(database: Database) {
            INST = database
        }

    }

}