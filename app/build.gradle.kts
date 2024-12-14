import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // Not Original
    id("com.google.gms.google-services")
}



android {
    namespace = "mobilesecurity.sit.project"
    compileSdk = 34

    defaultConfig {
        applicationId = "mobilesecurity.sit.project"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Not Original
        multiDexEnabled = true

        buildFeatures {
            viewBinding = true
        }
    }




    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res", "src\\main\\res\\layouts\\main",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\userauth", "src\\main\\res", "src\\main\\res\\layouts\\noti_chat",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\payments"
                )
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Not Original
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore-ktx")

    // For Glide
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    implementation ("com.makeramen:roundedimageview:2.3.0")

// For Picasso
    implementation ("com.squareup.picasso:picasso:2.8")

// Additional dependency testing
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.9")
    implementation ("com.google.android.gms:play-services-location:18.0.0")



//    implementation("com.android.support:cardview-v7:28.0.0")

}