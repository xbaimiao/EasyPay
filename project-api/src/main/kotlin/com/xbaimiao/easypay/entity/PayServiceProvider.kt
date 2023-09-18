package com.xbaimiao.easypay.entity

import com.xbaimiao.easylib.util.info

object PayServiceProvider {

    private val allService = ArrayList<PayService>()

    fun registerService(payService: PayService) {
        allService.add(payService)
        info("${javaClass.simpleName} register service ${payService.javaClass.simpleName}")
    }

    fun getService(name: String): PayService? {
        return getAllService().find { it.name == name }
    }

    inline fun <reified T> getService(clazz: Class<out T>): T? {
        return getAllService().find { it.javaClass.name == clazz.name } as? T
    }

    fun getAllService(): Collection<PayService> {
        return allService.toList()
    }

    fun clear() {
        allService.clear()
    }

}