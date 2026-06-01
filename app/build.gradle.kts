plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")

    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.deltacleaners"
    compileSdk = 35

    flavorDimensions += "role"

    productFlavors {

        create("customer") {
            dimension = "role"

            applicationIdSuffix = ".customer"
            versionNameSuffix = "-customer"

            resValue(
                "string",
                "app_name",
                "Delta Cleaners"
            )
        }

        create("cleaner") {
            dimension = "role"

            applicationIdSuffix = ".cleaner"
            versionNameSuffix = "-cleaner"

            resValue(
                "string",
                "app_name",
                "Delta Cleaner Partner"
            )
        }

        create("admin") {
            dimension = "role"

            applicationIdSuffix = ".admin"
            versionNameSuffix = "-admin"

            resValue(
                "string",
                "app_name",
                "Delta Cleaners Admin"
            )
        }
    }

    defaultConfig {
        applicationId = "com.example.deltacleaners"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    hilt {
        enableAggregatingTask = true
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    constraints {
        implementation(libs.androidx.concurrent.futures) {
            because("Conflict with transitive dependency from androidx.test.ext:junit")
        }
    }

    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.8.3"
    )

    implementation(
        "androidx.activity:activity-compose:1.9.0"
    )

    // Compose
    implementation(
        platform(
            "androidx.compose:compose-bom:2024.06.00"
        )
    )

    implementation("androidx.compose.ui:ui")

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )
    implementation(libs.androidx.ui)
    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )

    // Navigation
    implementation(
        "androidx.navigation:navigation-compose:2.7.7"
    )

    // ViewModel
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3"
    )

    // Hilt
    implementation(
        "com.google.dagger:hilt-android:2.57"
    )

    kapt(
        "com.google.dagger:hilt-compiler:2.57"
    )

    implementation(
        "androidx.hilt:hilt-navigation-compose:1.2.0"
    )

    // Coroutines
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1"
    )

    // Coil
    implementation(
        "io.coil-kt:coil-compose:2.7.0"
    )

    // Timber
    implementation(
        "com.jakewharton.timber:timber:5.0.1"
    )

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-base:18.10.0")

    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.57")
    kaptTest("com.google.dagger:hilt-compiler:2.57")

    // Instrumented Testing
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.57")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.57")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
