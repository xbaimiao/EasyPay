package com.xbaimiao.easypay.parameters

import com.xbaimiao.easypay.api.Item
import com.xbaimiao.easypay.entity.Order
import dev.rgbmc.expression.parameters.StringParameter
import org.bukkit.entity.Player

class PlayerParameter(parameter: StringParameter, val player: Player, val item: Item, val order: Order) :
    StringParameter(parameter.string)
