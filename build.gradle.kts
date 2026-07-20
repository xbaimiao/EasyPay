import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val ktVersion: String by project
val easylibVersion: String by project
val paperVersion: String by project

plugins {
    java
    id("com.gradleup.shadow")
    id("com.xbaimiao.easylib")
    kotlin("jvm")
}

group = "com.xbaimiao.easypay"
version = "1.3.6-SNAPSHOT"

easylib {
    env {
        mainClassName = "com.xbaimiao.easypay.EasyPay"
        pluginName = "EasyPay"
        kotlinVersion = ktVersion
        authors.add("xbaimiao")
        softDepend.addAll(listOf("PlaceholderAPI", "Vault", "PlayerPoints", "CMI", "XConomy"))
        updateInfo = "支持新版本 26.1"
        apiVersion = "26.1"
        foliaSupported = true
    }
    version = easylibVersion
    relocate("com.xbaimiao.easylib", "${project.group}.shadow.easylib", false)
    relocate("com.zaxxer.hikari", "${project.group}.shadow.hikari", false)
    relocate("com.j256.ormlite", "${project.group}.shadow.ormlite", false)
    relocate("kotlin", "${project.group}.shadow.kotlin", true)
    relocate("kotlinx", "${project.group}.shadow.kotlinx", true)
    relocate("org.java_websocket", "${project.group}.shadow.websocket", false)
    relocate("com.google.gson", "${project.group}.shadow.gson", true)
    relocate("dev.rgbmc.alipayconnector", "${project.group}.shadow.alipay_dlc", false)
    relocate("dev.rgbmc.walletconnector", "${project.group}.shadow.wechat", false)
    relocate("com.xbaimiao.ktor", "${project.group}.shadow.ktor", false)
    relocate("okhttp3", "${project.group}.shadow.okhttp3", false)
    relocate("okio", "${project.group}.shadow.okio", false)
    relocate("org.bouncycastle", "${project.group}.shadow.org.bouncycastle", false)
    relocate("com.alipay.api", "${project.group}.shadow.com.alipay.api", false)
    relocate("com.google.zxing", "${project.group}.shadow.zxing", false)
    relocate("_COROUTINE", "${project.group}.shadow.COROUTINE", false)
    relocate("com.wechat.pay.java", "${project.group}.shadow.wechat.pay", false)
    relocate("fuel", "${project.group}.shadow.fuel", false)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.fastmcmirror.org/content/repositories/releases/")
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly(fileTree("../libs"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        compileOnly("io.papermc.paper:paper-api:$paperVersion")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 8
    }
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation("com.xbaimiao.ktor:ktor-plugins-bukkit:1.1.1")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":project-api"))
    implementation("com.alipay.sdk:alipay-sdk-java:4.39.104.ALL")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    implementation("dev.rgbmc:WalletConnector:1.0.0-259dfb2")
    implementation("dev.rgbmc:AliPayConnector:1.0.0-18ef856")
    implementation("com.github.wechatpay-apiv3:wechatpay-java:0.2.12")

    compileOnly("com.stripe:stripe-java:25.12.0")
    compileOnly("com.paypal.sdk:checkout-sdk:2.0.0")

    implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")

    compileOnly("io.papermc.paper:paper-api:$paperVersion")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release = 8
    }
    withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
    }
    processResources {
        outputs.upToDateWhen { false }
    }
    shadowJar {
        dependsOn("generatePluginYml")
        dependencies {
            easylib.library.forEach {
                if (it.cloud) {
                    exclude(dependency(it.id))
                }
            }
            exclude(dependency("org.jetbrains:annotations:"))
            exclude(dependency("com.google.code.gson:gson:"))
            exclude(dependency("org.jetbrains.kotlin:"))
            exclude(dependency("org.jetbrains.kotlinx:"))
        }

        exclude("LICENSE")
        exclude("META-INF/*.SF")
        archiveClassifier.set("")

        easylib.relocate.forEach {
            relocate(it.pattern, it.replacement)
        }
        minimize()
    }
}
