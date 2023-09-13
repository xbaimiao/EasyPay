package com.xbaimiao.easypay.functions

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easypay.FunctionUtil.argumentArray
import com.xbaimiao.easypay.parameters.PlayerParameter
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult
import dev.rgbmc.expression.functions.FunctionResult.DefaultResult

@Suppress("DEPRECATION")
class TitleFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val player = playerParameter.player
        val arguments = playerParameter.string.argumentArray()
        when (arguments.size) {
            2 -> player.sendTitle(arguments[0].colored(), arguments[1].colored())
            5 -> player.sendTitle(
                arguments[0].colored(),
                arguments[1].colored(),
                arguments[2].toInt(),
                arguments[3].toInt(),
                arguments[4].toInt()
            )

            else -> return DefaultResult(FunctionResult.Status.FAILURE)
        }
        return DefaultResult(FunctionResult.Status.SUCCESS)
    }

    override fun getName(): String {
        return "title"
    }
}
