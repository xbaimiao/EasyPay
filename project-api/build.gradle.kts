import org.gradle.jvm.tasks.Jar
import java.net.URI

plugins {
    java
    `maven-publish`
    kotlin("jvm")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.xbaimiao.com/repository/maven-public/")
}

tasks.register("sourcesJar", Jar::class.java) {
    this.group = "build"
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven {
            credentials {
                username = "xbaimiao"
                password = "xbaimiao"
            }
            url = URI("https://repo.fastmcmirror.org/content/repositories/snapshots/")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.rootProject.group.toString()
            artifactId = project.rootProject.name
            version = "${project.rootProject.version}"
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}

