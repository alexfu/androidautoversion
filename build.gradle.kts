buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        mavenCentral()
    }
    dependencies {
        classpath("com.gradle.publish:plugin-publish-plugin:0.9.2")
    }
}

plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.serialization") version "1.3.61"
    `kotlin-dsl`
    `maven-publish`
}

group = "com.github.alexfu"
version = "3.2.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.google.com/dl/android/maven2")
    }
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
    implementation("com.android.tools.build:gradle:3.4.0")
}
