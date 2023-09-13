package com.xbaimiao.easypay.api

import com.xbaimiao.easylib.util.info

/**
 * ItemProvider
 *
 * @author xbaimiao
 * @since 2023/9/13 13:12
 */
object ItemProvider {

    private val allItem = ArrayList<Item>()

    fun register(item: Item) {
        allItem.add(item)
        info("${javaClass.simpleName} register item ${item.name}")
    }

    fun getItem(name: String): Item? {
        return getAllItem().find { it.name == name }
    }

    fun getAllItem(): Collection<Item> {
        return allItem.toList()
    }

}