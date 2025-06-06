plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.assemblepicture"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.assemblepicture"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.12.0");
    implementation("com.github.bumptech.glide:compiler:4.12.0");
    implementation("com.squareup.retrofit2:retrofit:2.9.0");
    implementation("com.squareup.retrofit2:converter-gson:2.9.0");
    implementation("androidx.cardview:cardview:1.0.0");
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0");

    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}