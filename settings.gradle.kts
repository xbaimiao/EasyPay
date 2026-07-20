pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.xbaimiao.com/repository/maven-public/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    val ktVersion: String by settings
    val shadowJarVersion: String by settings
    val easylibPluginVersion: String by settings
    plugins {
        kotlin("jvm") version ktVersion
        id("com.gradleup.shadow") version shadowJarVersion
        id("com.xbaimiao.easylib") version easylibPluginVersion
    }
}

rootProject.name = "EasyPay"
include("project-api")
