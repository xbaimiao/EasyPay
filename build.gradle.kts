plugins {
    java
    id("com.github.johnrengelman.shadow") version ("7.1.2")
    id("com.xbaimiao.easylib") version ("1.1.1")
    kotlin("jvm") version "1.7.10"
}

easylib {
    version = "3.4.3"
    nbt = false
    nbtVersion = "1.3.1"
    hikariCP = true
    ormlite = false
    userMinecraftLib = false
    minecraftVersion = "1.7.10"
    isPaper = false
}

group = "com.xbaimiao.easypay"
version = "1.1.6"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.fastmcmirror.org/content/repositories/releases/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly(fileTree("../libs"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    }

}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":project-api"))
    implementation("com.alipay.sdk:alipay-sdk-java:4.38.72.ALL")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.zxing:core:3.5.2")
    implementation("dev.rgbmc:WalletConnector:1.0.0-cc9b05d")
    implementation("com.xbaimiao.ktor:ktor-plugins-bukkit:1.1.0")
    implementation("com.github.wechatpay-apiv3:wechatpay-java:0.2.12")
    implementation("com.google.guava:guava:21.0")

    compileOnly(fileTree("libs"))
    //implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
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
        filesMatching("plugin.yml") {
            expand(*props.toTypedArray())
        }
    }
    shadowJar {
        dependencies {
//            exclude(dependency("org.slf4j:"))
            exclude(dependency("org.jetbrains:annotations:"))
            exclude(dependency("com.google.code.gson:gson:2.8.0"))
//            exclude(dependency("org.jetbrains.kotlin:"))
//            exclude(dependency("org.jetbrains:"))
        }

        exclude("LICENSE")
        exclude("META-INF/*.SF")
        archiveClassifier.set("")

        relocate("com.xbaimiao.easylib", "${project.group}.shadow.easylib")
        relocate("com.zaxxer.hikari", "${project.group}.shadow.hikari")
        relocate("com.j256.ormlite", "${project.group}.shadow.ormlite")
        relocate("kotlin", "${project.group}.shadow.kotlin")
        relocate("kotlinx", "${project.group}.shadow.kotlinx")
        relocate("org.java_websocket", "${project.group}.shadow.websocket")
        relocate("com.google.gson", "${project.group}.shadow.gson")
        relocate("dev.rgbmc.walletconnector", "${project.group}.shadow.wechat")
        relocate("com.xbaimiao.ktor", "${project.group}.shadow.ktor")
        relocate("javax.xml", "${project.group}.shadow.javax.xml")
        relocate("okhttp3", "${project.group}.shadow.okhttp3")
        relocate("okio", "${project.group}.shadow.okio")
        relocate("org.xml.sax", "${project.group}.shadow.org.xml.sax")
        relocate("org.w3c.dom", "${project.group}.shadow.org.w3c.dom")
        relocate("org.bouncycastle", "${project.group}.shadow.org.bouncycastle")
        relocate("com.alipay.api", "${project.group}.shadow.com.alipay.api")
        relocate("com.google.common", "${project.group}.shadow.common")
        relocate("com.google.zxing", "${project.group}.shadow.zxing")
        relocate("_COROUTINE", "${project.group}.shadow.COROUTINE")
        relocate("com.wechat.pay.java", "${project.group}.shadow.wechat.pay")
        minimize()
    }
}
