package com.xbaimiao.easypay.util

import com.xbaimiao.easylib.util.Dependency
import com.xbaimiao.easylib.util.DependencyList

@DependencyList(
    [Dependency(
        url = "net.kyori:adventure-api:4.16.0",
        clazz = "net.kyori.adventure.inventory.Book",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-key:4.16.0",
        clazz = "net.kyori.adventure.inventory.Book",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-nbt:4.16.0",
        clazz = "net.kyori.adventure.inventory.Book",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-text-minimessage:4.16.0",
        clazz = "net.kyori.adventure.text.minimessage.MiniMessage",
        format = true
    ), Dependency(
        url = "net.kyori:examination-api:1.3.0",
        clazz = "net.kyori.examination.Examinable",
        format = true
    ), Dependency(
        url = "net.kyori:examination-string:1.3.0",
        clazz = "net.kyori.examination.Examinable",
        format = true
    ), Dependency(
        url = "net.kyori:option:1.0.0",
        clazz = "net.kyori.option.Option",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-platform-api:4.3.2",
        clazz = "net.kyori.adventure.platform.bukkit.BukkitAudiences",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-platform-facet:4.3.2",
        clazz = "net.kyori.adventure.platform.bukkit.BukkitAudiences",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-platform-viaversion:4.3.2",
        clazz = "net.kyori.adventure.platform.bukkit.BukkitAudiences",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-platform-bukkit:4.3.2",
        clazz = "net.kyori.adventure.platform.bukkit.BukkitAudiences",
        format = true
    ), Dependency(
        url = "com.paypal.sdk:checkout-sdk:2.0.0",
        clazz = "com.paypal.core.PayPalEnvironment",
        format = true
    ), Dependency(
        url = "com.paypal:paypalhttp:2.0.0",
        clazz = "com.paypal.http.exceptions.HttpException",
        format = true
    ), Dependency(
        url = "com.google.code.gson:gson:2.10.1",
        clazz = "com.stripe.Stripe",
        format = true
    ), Dependency(
        url = "com.stripe:stripe-java:24.21.0",
        clazz = "com.stripe.Stripe",
        format = true
    )
    ]
)
object Dependencies