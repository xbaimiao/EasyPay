package com.xbaimiao.easypay.api

abstract class FunctionUtil {
    companion object {
        lateinit var instance: FunctionUtil
    }

    abstract fun getFastExpression(): Any
}