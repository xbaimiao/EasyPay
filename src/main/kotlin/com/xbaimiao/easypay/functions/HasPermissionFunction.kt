package com.xbaimiao.easypay.functions

import com.xbaimiao.easypay.parameters.PlayerParameter
import com.xbaimiao.easypay.results.CancellableResult
import dev.rgbmc.expression.functions.FastFunction
import dev.rgbmc.expression.functions.FunctionParameter
import dev.rgbmc.expression.functions.FunctionResult

class HasPermissionFunction : FastFunction {
    override fun call(parameter: FunctionParameter?): FunctionResult {
        val playerParameter = parameter!! as PlayerParameter
        val player = playerParameter.player
        return CancellableResult(player.hasPermission(playerParameter.string))
    }

    override fun getName(): String {
        return "hasPermission"
    }
}