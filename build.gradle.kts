plugins {
    java
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    kotlin("jvm") version "1.9.20"
}

group = "com.xbaimiao.easypay"
version = "1.2.4-CANARY_2"

val easyLibVersion = "3.8.5"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        credentials {
            username = project.findProperty("githubUsername").toString()
            password = project.findProperty("githubPassword").toString()
        }
        name = "GithubPackages"
        url = uri("https://maven.pkg.github.com/xbaimiao/EasyLib")
    }
    // Use Proxied Repo
    maven("https://repo.fastmcmirror.org/content/repositories/xbaimiao/")
    maven {
        url = uri("https://repo.fastmcmirror.org/content/repositories/releases/")
    }
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    repositories{
        mavenLocal()
        mavenCentral()
        // 不使用私有仓库获取依赖
        /*maven {
            credentials {
                username = project.findProperty("githubUsername").toString()
                password = project.findProperty("githubPassword").toString()
            }
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/xbaimiao/EasyLib")
        }*/
        // Use Proxied Repo
        maven("https://repo.fastmcmirror.org/content/repositories/xbaimiao/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.codemc.org/repository/maven-public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly("com.xbaimiao:easy-lib:$easyLibVersion")
        compileOnly(fileTree("../libs"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    }

}

dependencies {
    implementation("com.xbaimiao:easy-lib:$easyLibVersion")
    implementation("de.tr7zw:item-nbt-api:2.13.0")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":project-api"))
    implementation("com.alipay.sdk:alipay-sdk-java:4.39.104.ALL")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    implementation("dev.rgbmc:WalletConnector:1.0.0-3656068")
    implementation("dev.rgbmc:FastExpression:1.0.0-a0aa2c1")

    // EvalEx 2 for Java 8-10
    implementation("com.udojava:EvalEx:2.7")
    // EvalEx 3 for Java 11+
    implementation("com.ezylang:EvalEx:3.2.0")
    // Other resolution for if function
    implementation("com.creativewidgetworks:expression-evaluator:2.3.0")

    implementation("com.xbaimiao.ktor:ktor-plugins-bukkit:1.1.0")
    implementation("com.github.wechatpay-apiv3:wechatpay-java:0.2.12")

    implementation("com.comphenix.packetwrapper:PacketWrapper:1.20.4-2.3.0") {
        exclude(module = "spigot-api")
    }

    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.2")

    compileOnly("com.stripe:stripe-java:25.12.0")
    compileOnly("com.paypal.sdk:checkout-sdk:2.0.0")

    compileOnly("org.apache.groovy:groovy-all:4.0.17")

    implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")

    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    //implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("com.github.retrooper.packetevents:spigot:2.3.0")
    compileOnly("io.netty:netty-buffer:4.1.111.Final")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 8
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    processResources {
        val props = ArrayList<Pair<String, Any>>()
        props.add("version" to "${rootProject.version}")
        props.add("main" to "${project.rootProject.group}.${project.rootProject.name}")
        props.add("name" to project.rootProject.name)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(*props.toTypedArray())
        }
    }
    shadowJar {
        dependencies {
//            exclude(dependency("org.slf4j:"))
            exclude(dependency("org.jetbrains:annotations:"))
            exclude(dependency("com.google.code.gson:gson:"))
            exclude(dependency("org.jetbrains.kotlin:"))
            exclude(dependency("org.jetbrains.kotlinx:"))
        }

        exclude("LICENSE")
        exclude("META-INF/*.SF")
        archiveClassifier.set("")

        relocate("com.xbaimiao.easylib", "${project.group}.shadow.easylib")
        relocate("com.zaxxer.hikari", "${project.group}.shadow.hikari")
        relocate("com.j256.ormlite", "${project.group}.shadow.ormlite")
        relocate("de.tr7zw", "${project.group}.shadow.itemnbtapi")
        relocate("kotlin", "${project.group}.shadow.kotlin")
        relocate("kotlinx", "${project.group}.shadow.kotlinx")
        relocate("org.java_websocket", "${project.group}.shadow.websocket")
        relocate("com.google.gson", "${project.group}.shadow.gson")
        relocate("dev.rgbmc.walletconnector", "${project.group}.shadow.wechat")
        relocate("dev.rgbmc.expression", "${project.group}.shadow.expression")
        relocate("com.ezylang.evalex", "${project.group}.shadow.evalex3")
        relocate("com.udojava.evalex", "${project.group}.shadow.evalex2")
        relocate("com.creativewidgetworks.expressionparser", "${project.group}.shadow.expeval")
        relocate("com.xbaimiao.ktor", "${project.group}.shadow.ktor")
        //relocate("javax.xml", "${project.group}.shadow.javax.xml")
        relocate("okhttp3", "${project.group}.shadow.okhttp3")
        relocate("okio", "${project.group}.shadow.okio")
        //relocate("org.xml.sax", "${project.group}.shadow.org.xml.sax")
        //relocate("org.w3c.dom", "${project.group}.shadow.org.w3c.dom")
        relocate("org.bouncycastle", "${project.group}.shadow.org.bouncycastle")
        relocate("com.alipay.api", "${project.group}.shadow.com.alipay.api")
        relocate("com.google.zxing", "${project.group}.shadow.zxing")
        relocate("_COROUTINE", "${project.group}.shadow.COROUTINE")
        relocate("com.wechat.pay.java", "${project.group}.shadow.wechat.pay")
        relocate("com.comphenix.packetwrapper", "${project.group}.shadow.packets")
        //relocate("com.paypal", "${project.group}.shadow.paypal")
        //relocate("com.stripe", "${project.group}.shadow.stripe")
        relocate("fuel", "${project.group}.shadow.fuel")
        relocate("com.cryptomorin.xseries", "${project.group}.shadow.xseries")
        relocate("net.kyori.adventure", "${project.group}.shadow.adventure")
        minimize()
    }
}
