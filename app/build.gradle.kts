plugins {
    id("com.android.application")
}

android {
    namespace = "com.aeiou.widgetbar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aeiou.widgetbar"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        disable += "AndroidGradlePluginVersion"
    }
}

dependencies {
}
