plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    jvm("desktop")
    js(IR) { browser() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared:core:model"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting { dependencies { implementation(kotlin("test")) } }
        val androidMain by getting {
            dependencies {
                implementation(libs.retrofit)
                implementation(libs.okhttp)
                implementation(libs.logging.interceptor)
                implementation(libs.retrofit2.kotlinx.serialization.converter)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
    jvmToolchain(11)
}

android {
    namespace = "com.pusu.indexed.jikan"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
