package com.xbaimiao.easypay.book

import org.bukkit.entity.Player

/***
 * BookUtil 向可交互的支付方式提供链接跳转
 */
interface BookUtil {
    fun openBook(player: Player, price: String, url: String)

    fun closeBook(player: Player)
}