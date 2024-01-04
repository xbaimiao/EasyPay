package com.xbaimiao.easypay.api

interface CustomItemCreate {

    val name: String
    fun createItem(price: Double): Item
}