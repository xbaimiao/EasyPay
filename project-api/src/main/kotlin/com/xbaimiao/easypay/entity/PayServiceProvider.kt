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

    fun getAllService(): Collection<PayService> {
        return allService.toList()
    }

}