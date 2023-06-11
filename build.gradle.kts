import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.ktlint)
}

group = "com.jerryjeon"
version = "1.5.0"

tasks.test {
    useJUnitPlatform()
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.okio.okio)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.compose.material.icons)
    testImplementation(libs.jupiter)
    testImplementation(libs.kotest.assertions)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "18"
        targetCompatibility = "18"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "18"
    }
}

compose.desktop {
    application {
        mainClass = "com.jerryjeon.logjerry.MainKt"
    }
}
