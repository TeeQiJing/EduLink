plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }

    namespace = "com.dellmau.edulink"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dellmau.edulink"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.compose.material3:material3:1.3.1")


    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha05")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("androidx.activity:activity:1.9.3")

    implementation("com.google.firebase:firebase-firestore:25.1.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")


    // Scalable Size Unit (Support for different screen sizes), SDP - Scalable DP
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.intuit.ssp:ssp-android:1.0.6")

    // Rounded ImageView
    implementation("com.makeramen:roundedimageview:2.3.0")

    // Bcrypt
    implementation("org.mindrot:jbcrypt:0.4")

//    implementation("com.github.pengrad:echarts-android:1.9.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")





    // For loading images
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Image loading (Picasso)
    implementation("com.squareup.picasso:picasso:2.71828")

    // Firebase SDK
    implementation("com.google.firebase:firebase-core:21.1.1")



    implementation("com.google.android.material:material:1.12.0")

    implementation("com.github.bumptech.glide:glide:4.15.1") // or the latest version
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")// Glide compiler
    implementation("com.caverock:androidsvg:1.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")  // Add Apache POI for Excel file handling


    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
//    implementation("com.chaquo.python:chaquopy:12.1.0")

}