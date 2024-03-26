package com.xbaimiao.easypay.book

import com.xbaimiao.easylib.EasyPlugin
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.entity.Player

class DefaultBook(
    private val lines: List<String>
) : BookUtil {

    private val minimessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.clickEvent())
                .resolver(StandardTags.hoverEvent())
                .resolver(StandardTags.newline())
                .resolver(StandardTags.font())
                .resolver(StandardTags.keybind())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.transition())
                .build()
        )
        .build()
    private val adventure = BukkitAudiences.create(EasyPlugin.getPlugin())
    override fun openBook(player: Player, price: String, url: String) {
        val book = Book.builder()
            .author(Component.text("EasyPay"))
            .title(Component.text("EasyPay BookUI"))
            .pages(
                listOf(
                    minimessage.deserialize(
                        lines.joinToString("<newline>")
                            .replace("\${price}", price)
                            .replace("\${url}", url)
                    )
                )
            ).build()
        adventure.player(player).openBook(book)
    }

    override fun closeBook(player: Player) {
        player.openWorkbench(player.location, true)
        player.closeInventory()
    }
}