package com.xbaimiao.easypay.entity

/**
 * OrderStatus
 *
 * @author xbaimiao
 * @since 2023/9/12 23:08
 */
enum class OrderStatus {
    /**
     * 支付成功
     */
    SUCCESS,

    /**
     * 等待扫码
     */
    WAIT_SCAN,

    /**
     * 等待支付
     */
    WAIT_PAY,

    /**
     * 未知
     */
    UNKNOWN
}