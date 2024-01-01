package com.xbaimiao.easypay.item

import com.xbaimiao.easylib.bridge.player.parseECommand
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
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
    private val commands: List<String>,
    override val price: Double,
    override val name: String
) : AbstractItem() {

    override fun sendTo(player: Player, service: PayService, order: Order) {
        commands.parseECommand(player).exec(Bukkit.getConsoleSender())
//        FunctionUtil.parseActions(player, order, service, rewards)
    }

}

data class CustomPriceItemConfig(
    val min: Int,
    val max: Int,
    val ratio: Int,
    val commands: List<String>,
    val name: String
) {

    private fun replaceList(price: Double, name: String, list: List<String>): List<String> {
        val newList = list.toMutableList()
        newList.replaceAll {
            it.replace("%item_name%", name).replace("%custom_amount%", (price * ratio).roundToInt().toString())
        }
        return newList
    }

    fun createItem(price: Double): CustomPriceItem {
        return CustomPriceItem(
            replaceList(price, name, commands),
            price,
            name
        )
    }

}