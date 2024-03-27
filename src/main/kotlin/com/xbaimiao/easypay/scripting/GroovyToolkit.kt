package com.xbaimiao.easypay.scripting

import com.xbaimiao.easylib.bridge.replacePlaceholder
import org.bukkit.entity.Player

object GroovyToolkit {
    @JvmStatic
    fun parsePlaceholders(player: Player, content: String): String = content.replacePlaceholder(player)
}