import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}
//androidComponents {
//    onVariants { variant ->
//        variant.outputsDir
//        variant.outputs.forEach { output ->
//            //output.out
//        }
//    }
//}
android {
    namespace = "com.sosauce.cuteconnect"
    compileSdk {
        version = release(36)
    }

    val keystoreFile = file("release_key.jks")
    signingConfigs {
        create("release") {
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            } else {
                println("No keystore found, APK will be unsigned")
            }
        }
    }

    defaultConfig {
        applicationId = "com.sosauce.cuteconnect"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    applicationVariants.all {
//        val variant = this
//        variant.outputs
//            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
//            .forEach { output ->
//                val outputFileName = "CCNT_${variant.versionName}.apk"
//                output.outputFileName = outputFileName
//            }
//    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }


    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.startup)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.androidx.foundation.layout)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.cloudy)
    implementation(libs.colorpicker.compose)
    implementation(libs.material.kolor)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.coil.video)
    implementation(libs.geocoder)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.sweetselect.compose)
    implementation("androidx.paging:paging-compose:3.4.1")
    implementation("androidx.paging:paging-runtime:3.4.1")
    implementation(libs.sweetselect.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ez.vcard)
    implementation(project(":smsmms"))

}