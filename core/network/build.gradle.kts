plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
            }
        }
        val androidMain by getting { dependencies { implementation(libs.ktor.client.okhttp) } }
        val desktopMain by getting { dependencies { implementation(libs.ktor.client.okhttp) } }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies { implementation(libs.ktor.client.darwin) }
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

    }
    jvmToolchain(11)
}

android {
    namespace = "com.pusu.indexed.core.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
