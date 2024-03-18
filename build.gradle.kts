plugins {
    java
    id("com.github.johnrengelman.shadow") version ("7.1.2")
    kotlin("jvm") version "1.7.10"
}

group = "com.xbaimiao.easypay"
version = "1.2.1"

repositories {
    mavenCentral()
    maven {
        credentials {
            username = project.findProperty("githubUsername").toString()
            password = project.findProperty("githubPassword").toString()
        }
        name = "GithubPackages"
        url = uri("https://maven.pkg.github.com/xbaimiao/EasyLib")
    }
    maven {
        url = uri("https://repo.fastmcmirror.org/content/repositories/releases/")
    }
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    repositories{
        mavenCentral()
        maven {
            credentials {
                username = project.findProperty("githubUsername").toString()
                password = project.findProperty("githubPassword").toString()
            }
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/xbaimiao/EasyLib")
        }
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.codemc.org/repository/maven-public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly("com.xbaimiao:easy-lib:3.5.7")
        compileOnly(fileTree("../libs"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    }

}

dependencies {
    implementation("com.xbaimiao:easy-lib:3.5.7")
    implementation("de.tr7zw:item-nbt-api:2.12.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":project-api"))
    implementation("com.alipay.sdk:alipay-sdk-java:4.38.72.ALL")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    implementation("dev.rgbmc:WalletConnector:1.0.0-cc9b05d")
    implementation("dev.rgbmc:FastExpression:1.0.0-a0aa2c1")

    // EvalEx 2 for Java 8-10
    implementation("com.udojava:EvalEx:2.7")
    // EvalEx 3 for Java 11+
    implementation("com.ezylang:EvalEx:3.0.5")
    // Other resolution for if function
    implementation("com.creativewidgetworks:expression-evaluator:2.3.0")

    implementation("com.xbaimiao.ktor:ktor-plugins-bukkit:1.1.0")
    implementation("com.github.wechatpay-apiv3:wechatpay-java:0.2.12")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    //implementation("com.google.code.gson:gson:2.10.1")
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
        relocate("javax.xml", "${project.group}.shadow.javax.xml")
        relocate("okhttp3", "${project.group}.shadow.okhttp3")
        relocate("okio", "${project.group}.shadow.okio")
        relocate("org.xml.sax", "${project.group}.shadow.org.xml.sax")
        relocate("org.w3c.dom", "${project.group}.shadow.org.w3c.dom")
        relocate("org.bouncycastle", "${project.group}.shadow.org.bouncycastle")
        relocate("com.alipay.api", "${project.group}.shadow.com.alipay.api")
        relocate("com.google.zxing", "${project.group}.shadow.zxing")
        relocate("_COROUTINE", "${project.group}.shadow.COROUTINE")
        relocate("com.wechat.pay.java", "${project.group}.shadow.wechat.pay")
        minimize()
    }
}
