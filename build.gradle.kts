import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    java
    id("com.github.johnrengelman.shadow") version ("7.1.2")
    id("com.xbaimiao.easylib") version ("1.1.0")
    kotlin("jvm") version "1.7.10"
}

easylib {
    version = "3.0.5"
    nbt = false
    hikariCP = false
    ormlite = false
    userMinecraftLib = true
    minecraftVersion = "1.12.2"
    isPaper = false
}

group = "com.xbaimiao.easypay"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.fastmcmirror.org/content/repositories/releases/")
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
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    }

}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":project-api"))
    implementation("com.alipay.sdk:alipay-sdk-java:4.38.72.ALL")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("com.google.zxing:core:3.5.2")
    implementation("dev.rgbmc:WalletConnector:1.0.0-0a852a9")
    implementation("dev.rgbmc:FastExpression:1.0.0-a0aa2c1")
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
        props.add("version" to "${LocalDate.now().format(DateTimeFormatter.ofPattern("y.M.d"))}-${rootProject.version}")
        props.add("main" to "${project.rootProject.group}.${project.rootProject.name}")
        props.add("name" to project.rootProject.name)
        expand(*props.toTypedArray())
    }
    shadowJar {
        dependencies {
            exclude(dependency("org.slf4j:"))
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
        relocate("de.tr7zw", "${project.group}.shadow.itemnbtapi")
        relocate("kotlin", "${project.group}.shadow.kotlin")
        relocate("kotlinx", "${project.group}.shadow.kotlinx")
        relocate("org.java_websocket", "${project.group}.shadow.websocket")
        relocate("com.google.gson", "${project.group}.shadow.gson")
        relocate("dev.rgbmc.walletconnector", "${project.group}.shadow.wechat")
        minimize()
    }
}
