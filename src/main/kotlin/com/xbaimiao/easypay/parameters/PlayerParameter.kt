package com.xbaimiao.easypay.parameters

import dev.rgbmc.expression.parameters.StringParameter
import org.bukkit.entity.Player

class PlayerParameter(parameter: StringParameter, val player: Player) : StringParameter(parameter.string)
