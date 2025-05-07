val kspVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.25"
    `maven-publish`
}

group = "io.kotgres"
version = "0.1.4"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    // kotgres dsl
    implementation("com.github.kotgres:kotgres-dsl:v0.1.4")
    // metaprogramming
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
    // database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("commons-dbutils:commons-dbutils:1.8.0")
    // connection pooling
    implementation("com.zaxxer:HikariCP:5.0.1")
    // json
    implementation("org.json:json:20240303")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // logging
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
}

kotlin {
    jvmToolchain(11)
}

java {
    withSourcesJar()
    withJavadocJar()
}

/**
 * JITPACK PUBLISHING
 */
afterEvaluate {
    publishing {
        publications {
            create("java", MavenPublication::class) {
                from(components["java"])

                groupId = "io.kotgres"
                artifactId = "kotgres"
                version = "0.1.4"
            }
        }
    }
}


/**
 * MAVEN PUBLISHING (TODO)
 */
