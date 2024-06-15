package com.xbaimiao.easypay.util

import com.xbaimiao.easylib.util.Dependency
import com.xbaimiao.easylib.util.DependencyList

@DependencyList(
    [Dependency(
        url = "net.kyori:adventure-api:4.17.0",
        clazz = "net.kyori.adventure.Adventure",
        format = true,
        fetchDependencies = true,
        relocationRules = [
            "net!.kyori!.adventure",
            "com.xbaimiao.easypay.shadow.adventure"
        ]
    )/*, Dependency(
        url = "net.kyori:adventure-key:4.16.0",
        clazz = "net.kyori.adventure.key.Key",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-nbt:4.16.0",
        clazz = "net.kyori.adventure.nbt.Tokens",
        format = true
    )*/, Dependency(
        url = "net.kyori:adventure-text-minimessage:4.17.0",
        clazz = "net.kyori.adventure.text.minimessage.MiniMessage",
        format = true,
        fetchDependencies = true,
        relocationRules = [
            "net!.kyori!.adventure",
            "com.xbaimiao.easypay.shadow.adventure"
        ]
    )/*, Dependency(
        url = "net.kyori:adventure-text-serializer-gson:4.16.0",
        clazz = "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-text-serializer-gson-legacy-impl:4.16.0",
        clazz = "net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-text-serializer-bungeecord:4.3.2",
        clazz = "net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-text-serializer-json:4.16.0",
        clazz = "net.kyori.adventure.text.serializer.json.JsonComponentSerializer",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-text-serializer-json-legacy-impl:4.16.0",
        clazz = "net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-text-serializer-legacy:4.16.0",
        clazz = "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer",
        format = true
    ), Dependency(
        url = "net.kyori:examination-api:1.3.0",
        clazz = "net.kyori.examination.Examinable",
        format = true
    ), Dependency(
        url = "net.kyori:examination-string:1.3.0",
        clazz = "net.kyori.examination.string.Strings",
        format = true
    ), Dependency(
        url = "net.kyori:option:1.0.0",
        clazz = "net.kyori.option.Option",
        format = true
    ), Dependency(
        url = "net.kyori:adventure-platform-api:4.3.2",
        clazz = "net.kyori.adventure.platform.AudienceProvider",
        format = true
    )*/, Dependency(
        url = "net.kyori:adventure-platform-facet:4.3.2",
        clazz = "net.kyori.adventure.platform.facet.Facet",
        format = true,
        relocationRules = [
            "net!.kyori!.adventure",
            "com.xbaimiao.easypay.shadow.adventure"
        ]
    ), Dependency(
        url = "net.kyori:adventure-platform-viaversion:4.3.2",
        clazz = "net.kyori.adventure.platform.viaversion.ViaFacet",
        format = true,
        relocationRules = [
            "net!.kyori!.adventure",
            "com.xbaimiao.easypay.shadow.adventure"
        ]
    ), Dependency(
        url = "net.kyori:adventure-platform-bukkit:4.3.2",
        clazz = "net.kyori.adventure.platform.bukkit.BukkitAudiences",
        format = true,
        fetchDependencies = true,
        relocationRules = [
            "net!.kyori!.adventure",
            "com.xbaimiao.easypay.shadow.adventure"
        ]
    ), Dependency(
        url = "com.paypal.sdk:checkout-sdk:2.0.0",
        clazz = "com.paypal.core.PayPalEnvironment",
        format = true,
        fetchDependencies = true
    )/*, Dependency(
        url = "com.paypal:paypalhttp:2.0.0",
        clazz = "com.paypal.http.exceptions.HttpException",
        format = true
    )*/, Dependency(
        url = "com.stripe:stripe-java:25.9.0",
        clazz = "com.stripe.Stripe",
        format = true,
        fetchDependencies = true
    )]
)
object Dependencies
