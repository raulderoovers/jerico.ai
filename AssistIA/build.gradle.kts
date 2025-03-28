// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// Read API key
var apiUrl: String = localProperties.getProperty("assistant.service.url") ?: "default_value"
val apiKey: String = localProperties.getProperty("assistant.service.key") ?: "default_value"

//Service Mock Mode
val isMocked: String = localProperties.getProperty("assistant.service.mocked")?:"true"
//Splash Screen
val splashDuration: String = localProperties.getProperty("assistant.splash.duration")?: "0L"

// Pass the API key to the Module-level build.gradle.kts
subprojects {
    extra["assistant.service.url"] = apiUrl
    extra["assistant.service.key"] = apiKey
    extra["assistant.service.mocked"] = isMocked
    extra["assistant.splash.duration"] = splashDuration
}