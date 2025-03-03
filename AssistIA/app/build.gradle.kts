plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.assistia"
    compileSdk = 35
    buildFeatures.buildConfig = true

    applicationVariants.all {
        outputs.all {
            val outputFileName = when(buildType.name) {
                "release" -> "assistia.apk"
                else -> "assistia-debug.apk"
            }
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).setOutputFileName(outputFileName)
        }
    }

    defaultConfig {
        applicationId = "com.assistia"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "ASSISTANT_SERVICE_URL", "\"${project.extra["assistant.service.url"]}\"")
            buildConfigField("String", "ASSISTANT_SERVICE_KEY", "\"${project.extra["assistant.service.key"]}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "ASSISTANT_SERVICE_URL", "\"${project.extra["assistant.service.url"]}\"")
            buildConfigField("String", "ASSISTANT_SERVICE_KEY", "\"${project.extra["assistant.service.key"]}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}