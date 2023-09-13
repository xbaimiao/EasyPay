package com.xbaimiao.easypay

import com.xbaimiao.easypay.parameters.PlayerParameter
import dev.rgbmc.expression.FastExpression
import dev.rgbmc.expression.functions.CallableFunction
import dev.rgbmc.expression.parameters.StringParameter
import org.bukkit.entity.Player


object FunctionUtil {
    val functionManager = FastExpression()

    fun parseFunctions(manager: FastExpression, player: Player, expression: String): List<CallableFunction> {
        val callableFunctions = manager.functionManager.parseExpression(expression)
        for (function in callableFunctions) {
            function.parameter = PlayerParameter((function.parameter as StringParameter), player)
        }
        return callableFunctions
    }

    fun String.reformatArgument(): String {
        return this.replace(" ,", ",").replace(", ", ",")
    }

    fun String.argumentArray(): List<String> {
        return this.reformatArgument().split(",")
    }
}