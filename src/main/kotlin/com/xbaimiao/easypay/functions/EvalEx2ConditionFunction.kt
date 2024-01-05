package com.xbaimiao.easypay.functions

import com.udojava.evalex.Expression
import com.udojava.evalex.ExpressionSettings
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult
import java.math.BigDecimal
import java.math.MathContext

class EvalEx2ConditionFunction : FastFunction {
    private val settings = ExpressionSettings
        .builder()
        .mathContext(MathContext.DECIMAL32)
        .build()

    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val expression = Expression(playerParameter.string, settings)
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