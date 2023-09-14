package com.xbaimiao.easypay.functions

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class MessageFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val player = playerParameter.player
        player.sendMessage(playerParameter.string.colored())
        return CancellableResult(true)
    }

    override fun getName(): String {
        return "message"
    }
}