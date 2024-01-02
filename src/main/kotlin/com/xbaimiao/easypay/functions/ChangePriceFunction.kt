package com.xbaimiao.easypay.functions

import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class ChangePriceFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        playerParameter.order.price = playerParameter.string.toDouble()
        return CancellableResult(true)
    }

    override fun getName(): String {
        return "price"
    }
}