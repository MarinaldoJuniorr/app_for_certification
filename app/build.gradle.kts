import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.androidx.navigation.safeargs)
}

android {
    namespace = "com.example.app_for_certification"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.example.app_for_certification"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildFeatures {
            viewBinding = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        //noinspection WrongGradleMethod
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.livedata.ktx)
        implementation(libs.androidx.fragment.ktx)
        implementation(libs.androidx.swiperefreshlayout)
        implementation(libs.retrofit)
        implementation(libs.retrofit.gson)
        implementation(libs.okhttp.logging)
        implementation(libs.glide)
        kapt(libs.glide.compiler)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
}
