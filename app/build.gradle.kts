
// ===== app/build.gradle.kts =====
    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.compose)
        alias(libs.plugins.hilt)
        kotlin("kapt")
        alias(libs.plugins.kotlin.android)
    }

    android {
        namespace = "com.py.ani_nderesarai"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.py.ani_nderesarai"
            minSdk = 24
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables {
                useSupportLibrary = true
            }
        }

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
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.4"
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.material.icons.extended)

// Room
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        implementation(libs.androidx.room.compiler)

// Navigation
        implementation(libs.androidx.navigation.compose)

// WorkManager
        implementation(libs.androidx.work.runtime.ktx)

// Hilt
        implementation(libs.hilt.android)
        implementation(libs.hilt.compiler)
        implementation(libs.androidx.hilt.navigation.compose)
        implementation(libs.androidx.hilt.work)

// Permisos
        implementation(libs.accompanist.permissions)
        implementation(libs.core.ktx)

        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }
