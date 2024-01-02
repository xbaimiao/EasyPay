package com.xbaimiao.easypay.functions

import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class CancelOrderFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val result = CancellableResult(true)
        result.shouldCancel = true
        return result
    }

    override fun getName(): String {
        return "cancel"
    }
}