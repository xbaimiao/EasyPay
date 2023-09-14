package com.xbaimiao.easypay

import com.xbaimiao.easypay.api.CommandItem
import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
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

    fun parseActions(player: Player, item: Item, service: PayService) {
        if (item is CommandItem) {
            for (line in item.actions) {
                val newLine = line.formatVariables("service", service.name, "player", player.name, "price", item.price)
                for (function in parseFunctions(functionManager, player, newLine)) {
                    val cancelableResult = function.callFunction() as CancellableResult
                    if (!cancelableResult.status) break
                    if (cancelableResult.isCancelled) return
                }
            }
        } else {
            throw IllegalArgumentException("This item type not support actions feature")
        }
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