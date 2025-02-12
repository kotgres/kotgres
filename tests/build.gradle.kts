plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.noarg") version "1.9.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    // kotgres
    implementation(project(":core"))
    kspTest(project(":core"))
    // kotgres dsl
    implementation("com.github.kotgres:kotgres-dsl:0.1.1")
    // postgres driver
    implementation("org.postgresql:postgresql:42.6.0")
    // json
    implementation("org.json:json:20240303")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}


noArg {
    annotation("io.kotgres.orm.annotations.Table")
}
