package com.xbaimiao.easypay.util

import com.xbaimiao.easylib.util.Dependency
import com.xbaimiao.easylib.util.DependencyList

@DependencyList(
    [
        Dependency(
            url = "com.paypal.sdk:checkout-sdk:2.0.0",
            clazz = "com.paypal.core.PayPalEnvironment",
            format = true,
            fetchDependencies = true
        ),
        Dependency(
            url = "com.stripe:stripe-java:25.12.0",
            clazz = "com.stripe.Stripe",
            format = true,
            fetchDependencies = true
        )
    ]
)
object Dependencies
