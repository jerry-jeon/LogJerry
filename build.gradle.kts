import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.compose") version "1.2.0-beta01"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.0"
}

group = "com.jerryjeon"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.2.0-beta01")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.4.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
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
            targetFormats(TargetFormat.Dmg)
            packageName = "LogJerry"
            packageVersion = "1.0.0"

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
