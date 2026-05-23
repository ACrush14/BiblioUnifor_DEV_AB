import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.bibliounifornew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bibliounifornew"
        minSdk = 28
        targetSdk = 35
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {

    // Importa a Bill of Materials (BoM) do Firebase para alinhar as versões
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Adiciona a dependência do Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    // Android UI e Core
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Firebase (BOM garante compatibilidade entre todas as libs do Firebase)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Coroutines para Firebase (.await)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Coil para carregamento de imagens por URL
    implementation("io.coil-kt:coil:2.7.0")

    implementation("com.google.firebase:firebase-messaging:23.4.1") // ou versão mais recente sugerida pelo Android Studio
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    // Retrofit para chamadas de internet
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Converte o resultado para objetos Kotlin

    implementation("com.google.firebase:firebase-firestore")
    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
