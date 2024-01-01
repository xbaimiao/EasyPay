package com.xbaimiao.easypay.functions

import com.udojava.evalex.Expression
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult
import java.math.BigDecimal

class EvalEx2ConditionFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val expression = Expression(playerParameter.string)
        val expressionValue = expression.eval()
        if (expressionValue.compareTo(BigDecimal.ONE) == 0) {
            return CancellableResult(true)
        }
        return CancellableResult(false)
    }

    override fun getName(): String {
        return "if"
    }
}