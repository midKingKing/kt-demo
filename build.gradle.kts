import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
//    id("org.jetbrains.kotlin.jvm") version("1.3.41")
    kotlin("jvm") version ("1.3.50")
}

group = "local-kt"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    maven("https://maven.aliyun.com/repository/central")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    //testCompile("junit:junit:4.12")
    testCompile(group = "junit", name = "junit", version = "4.12")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks

compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}