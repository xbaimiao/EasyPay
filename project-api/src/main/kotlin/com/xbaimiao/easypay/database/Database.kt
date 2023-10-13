package com.xbaimiao.easypay.database

/**
 * Database
 *
 * @author xbaimiao
 * @since 2023/9/13 23:23
 */
interface Database {

    fun getAllOrder(): Collection<OrderData>

    /**
     * 获取玩家支付的所有订单
     */
    fun getAllOrder(playerName: String): Collection<OrderData>

    /**
     * 为玩家添加一个订单
     */
    fun addOrder(playerName: String, order: OrderData)

    /**
     * 添加累充金额
     */
    fun addRewardPrice(playerName: String, num: Double)

    /**
     * 能否领取这个奖励
     */
    fun canGetReward(playerName: String, reward: String): Boolean

    /**
     * 设置这个奖励为已领取
     */
    fun setGetReward(playerName: String, reward: String)

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