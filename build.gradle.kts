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
    relocate("com.google.gson", "${project.group}.shadow.gson", true)
    relocate("com.xbaimiao.ktor", "${project.group}.shadow.ktor", false)
    relocate("com.xbaimiao.baipay", "${project.group}.shadow.baipay", false)
    relocate("okhttp3", "${project.group}.shadow.okhttp3", false)
    relocate("okio", "${project.group}.shadow.okio", false)
    relocate("org.bouncycastle", "${project.group}.shadow.org.bouncycastle", false)
    relocate("com.alipay.api", "${project.group}.shadow.com.alipay.api", false)
    relocate("com.google.zxing", "${project.group}.shadow.zxing", false)
    relocate("_COROUTINE", "${project.group}.shadow.COROUTINE", false)
    relocate("com.wechat.pay.java", "${project.group}.shadow.wechat.pay", false)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.fastmcmirror.org/content/repositories/releases/")
    maven("https://maven.xbaimiao.com/repository/maven-private/") {
        credentials {
            username = providers.gradleProperty("BaiUser").orNull ?: System.getenv("BAI_MAVEN_USER")
            password = providers.gradleProperty("BaiPassword").orNull ?: System.getenv("BAI_MAVEN_PASSWORD")
        }
    }
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
    implementation("com.github.wechatpay-apiv3:wechatpay-java:0.2.12")
    implementation("com.xbaimiao.baipay:baipay-sdk:2.0.0")

    compileOnly("io.papermc.paper:paper-api:$paperVersion")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
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
