plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")

}

android {
    namespace = "avito.testtask.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.room.ktx)
//    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx.v260)
    kapt(libs.androidx.room.compiler)


    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.aws.android.sdk.s3)
    implementation(libs.aws.android.sdk.core)

    implementation(libs.androidx.security.crypto)
    implementation(libs.aws.android.sdk.s3.v2730)
    implementation(libs.aws.android.sdk.core.v2730)

    implementation(libs.pdfbox.android)
//    implementation(libs.epublib)
//    implementation(libs.jsoup)
//    implementation(libs.zip4j)

    implementation(project(":domain"))
}