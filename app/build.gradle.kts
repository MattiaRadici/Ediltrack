plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" // o la tua versione di Kotlin
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.ediltrack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ediltrack"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")

    //db supabase dipendenze
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-android:3.2.3")
    //auth
    implementation("io.github.jan-tennert.supabase:auth-kt")

    //libs grafiche
    implementation(libs.androidx.recyclerview)
    implementation ("com.google.android.material:material:1.12.0")

    //libs serializzazione
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    //libs per caricamento immagine
    implementation("io.coil-kt:coil:2.5.0")

}