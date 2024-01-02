package com.xbaimiao.easypay.item

import com.xbaimiao.easylib.bridge.player.parseECommand
import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easypay.api.CustomItemCreate
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.util.FunctionUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.math.roundToInt

/**
 * CustomConfiguration
 *
 * @author xbaimiao
 * @since 2023/9/19 12:58
 */
object CustomConfiguration {

    private var customPriceItemConfig: CustomPriceItemConfig? = null

    fun setCustomPriceItemConfig(customPriceItemConfig: CustomPriceItemConfig) {
        this.customPriceItemConfig = customPriceItemConfig
    }

    fun getCustomPriceItemConfig(): CustomPriceItemConfig {
        return customPriceItemConfig!!
    }

}

data class CustomPriceItem(
    private val actions: List<String>,
    private val preActions: List<String>,
    private val rewards: List<String>,
    private val commands: List<String>,
    override val price: Double,
    override val name: String
) : AbstractItem(actions, preActions) {

    override fun sendTo(player: Player, service: PayService, order: Order): Collection<String> {
        commands.parseECommand(player).exec(Bukkit.getConsoleSender())
        FunctionUtil.parseActions(player, order, service, rewards)
        return commands.map { it.replace("%player_name%", player.name).replacePlaceholder(player) }
    }

}

data class CustomPriceItemConfig(
    val min: Int,
    val max: Int,
    val ratio: Int,
    val actions: List<String>,
    val preActions: List<String>,
    val rewards: List<String>,
    val commands: List<String>,
    override val name: String
) : CustomItemCreate {

    private fun replaceList(price: Double, name: String, list: List<String>): List<String> {
        val newList = list.toMutableList()
        newList.replaceAll {
            it.replace("%item_name%", name).replace("%custom_amount%", (price * ratio).roundToInt().toString())
        }
        return newList
    }

    override fun createItem(price: Double): CustomPriceItem {
        return CustomPriceItem(
            replaceList(price, name, actions),
            replaceList(price, name, preActions),
            replaceList(price, name, rewards),
            replaceList(price, name, commands),
            price,
            name
        )
    }

}