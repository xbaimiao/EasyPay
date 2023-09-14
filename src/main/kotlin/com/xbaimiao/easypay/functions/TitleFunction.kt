package com.xbaimiao.easypay.functions

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easypay.FunctionUtil.argumentArray
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

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

            else -> return CancellableResult(false)
        }
        return CancellableResult(true)
    }

    override fun getName(): String {
        return "title"
    }
}
