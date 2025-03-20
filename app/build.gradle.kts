plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.recipe_rcm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.recipe_rcm"
        minSdk = 24
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    viewBinding{
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.google.code.gson:gson:2.8.8")

    //Glide 버전과 컴파일러 추가
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation("com.google.android.material:material:1.8.0")

    //navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    //firebase realtime database
    implementation ("com.google.firebase:firebase-firestore:24.5.0") // 최신 버전 확인
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))

    implementation ("com.google.firebase:firebase-database:20.0.5")
    implementation ("com.google.firebase:firebase-auth:21.0.8")
    implementation ("com.google.firebase:firebase-core:17.2.3")

    //firebase cloud messaging
    implementation ("androidx.work:work-runtime:2.7.1")
    implementation ("com.google.firebase:firebase-messaging:24.0.0")
    implementation ("androidx.work:work-runtime-ktx:2.8.1")

    // AndroidX CameraX
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")

    implementation ("androidx.camera:camera-video:1.3.0")
    implementation ("androidx.camera:camera-extensions:1.3.0")


    implementation ("androidx.core:core:1.9.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation ("org.tensorflow:tensorflow-lite:2.8.0")

}
