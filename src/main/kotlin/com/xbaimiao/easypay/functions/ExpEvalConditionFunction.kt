package com.xbaimiao.easypay.functions

import com.creativewidgetworks.expressionparser.Parser
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class ExpEvalConditionFunction : FastFunction {
    private val parser = Parser()

    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val value = parser.eval(playerParameter.string)
        if (value.asBoolean()) {
            return CancellableResult(true)
        }
        return CancellableResult(false)
    }

    override fun getName(): String {
        return "if"
    }
}