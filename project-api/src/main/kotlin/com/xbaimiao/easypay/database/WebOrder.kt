package com.xbaimiao.easypay.database

data class WebOrder(
    // 创建时间
    val createTime: Long,
    // 支付时间
    var payTime: Long,
    // 发货时间
    var sendTime: Long,
    // 商品描述
    val desc: String,
    // 订单号
    val orderId: String,
    // 交易方式
    val payType: String,
    // 商品价格
    val price: Double,
    // 玩家名称
    val player: String,
    // 交易状态
    var status: Status,
    // 发货日志
    var sendLog: String
) {

    enum class Status {
        WAIT, SUCCESS, TIMEOUT, WAIT_DELIVERY
    }

}