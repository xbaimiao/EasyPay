package com.xbaimiao.easypay.util

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.FastExpression
import dev.rgbmc.expression.functions.CallableFunction
import dev.rgbmc.expression.parameters.StringParameter
import org.bukkit.entity.Player


object FunctionUtil {
    val functionManager = FastExpression()

    private fun parseFunctions(
        manager: FastExpression,
        player: Player,
        expression: String,
        order: Order
    ): List<CallableFunction> {
        val callableFunctions = manager.functionManager.parseExpression(expression)
        for (function in callableFunctions) {
            function.parameter = PlayerParameter((function.parameter as StringParameter), player, order.item, order)
        }
        return callableFunctions
    }

    fun parseActions(player: Player, order: Order, service: PayService, expressions: List<String>): Boolean {
        for (line in expressions) {
            val newLine = line.formatVariables("service", service.name, "player", player.name, "price", order.price)
                .replacePlaceholder(player)
            for (function in parseFunctions(functionManager, player, newLine, order)) {
                val cancelableResult = function.callFunction() as CancellableResult
                if (!cancelableResult.status) break
                if (cancelableResult.isCancelled) return true
                if (cancelableResult.shouldCancel) return false
            }
        }
        return true
    }

    private fun String.reformatArgument(): String {
        return this.replace(" ,", ",").replace(", ", ",")
    }

    private fun String.formatVariables(vararg values: Any): String {
        return if (values.size % 2 == 0) {
            var temp = this
            for (i in (values.indices step 2)) {
                temp = temp.replace("$" + values[i], values[i + 1].toString())
            }
            temp
        } else {
            this
        }
    }

    fun String.argumentArray(): List<String> {
        return this.reformatArgument().split(",")
    }
}