package com.xbaimiao.easypay.functions

import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class ReturnFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val result = CancellableResult(true)
        result.isCancelled = true
        return result
    }

    override fun getName(): String {
        return "return"
    }
}