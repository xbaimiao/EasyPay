package com.xbaimiao.easypay.functions

import com.ezylang.evalex.Expression
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class ConditionFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val expression = Expression(playerParameter.string)
        val expressionValue = expression.evaluate()
        if (expressionValue.booleanValue) {
            return CancellableResult(true)
        }
        return CancellableResult(false)
    }

    override fun getName(): String {
        return "if"
    }
}