pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven("https://maven.xbaimiao.com/repository/maven-public/")
    }
}
rootProject.name = "EasyPay"
include("project-api")
