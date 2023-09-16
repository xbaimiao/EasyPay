package com.xbaimiao.easypay.parameters

import com.xbaimiao.easypay.api.Item
import dev.rgbmc.expression.parameters.StringParameter
import org.bukkit.entity.Player

class PlayerParameter(parameter: StringParameter, val player: Player, val item: Item) :
    StringParameter(parameter.string)
