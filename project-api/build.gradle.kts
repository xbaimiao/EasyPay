import org.gradle.jvm.tasks.Jar
import java.net.URI

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.register("sourcesJar", Jar::class.java) {
    this.group = "build"
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
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

