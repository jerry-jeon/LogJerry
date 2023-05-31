import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.ktlint)
}

group = "com.jerryjeon"
version = "1.3.0"

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


tasks.test {
    useJUnitPlatform()
}

val signingProperties = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "signing.properties")))
}

compose.desktop {
    application {
        javaHome = System.getenv("JDK_18")
        mainClass = "com.jerryjeon.logjerry.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LogJerry"

            macOS {
                iconFile.set(project.file("LogJerry.icns"))
                bundleID = "com.jerryjeon.logjerry"
                signing {
                    sign.set(signingProperties.getProperty("compose.desktop.mac.sign").toBoolean())
                    identity.set(signingProperties.getProperty("compose.desktop.mac.signing.identity"))
                }
                notarization {
                    appleID.set(signingProperties.getProperty("compose.desktop.mac.notarization.appleID"))
                    password.set(signingProperties.getProperty("compose.desktop.mac.notarization.password"))
                }
                appStore = signingProperties.getProperty("compose.desktop.mac.appStore").toBoolean()
                // provisioningProfile.set(project.file(signingProperties.getProperty("compose.desktop.mac.provisionProfile")))
                // runtimeProvisioningProfile.set(project.file(signingProperties.getProperty("compose.desktop.mac.runtimeProvisionProfile")))
            }
        }
    }
}
