import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.junit5)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.example.news_app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.news_app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/LICENSE*"
        )
    }

    flavorDimensions += "source"
    productFlavors {
        create("bbc") {
            dimension = "source"
            applicationIdSuffix = ".bbc"
            resValue("string", "app_name", "News App (BBC)")
            buildConfigField("String", "NEWS_SOURCE_ID", "\"bbc-news\"")
            buildConfigField("String", "NEWS_SOURCE_NAME", "\"BBC News\"")
            buildConfigField("String", "NEWS_API_KEY", "\"${readNewsApiKey()}\"")
        }
        create("abc") {
            dimension = "source"
            applicationIdSuffix = ".abc"
            resValue("string", "app_name", "News App (ABC)")
            buildConfigField("String", "NEWS_SOURCE_ID", "\"abc-news\"")
            buildConfigField("String", "NEWS_SOURCE_NAME", "\"ABC News\"")
            buildConfigField("String", "NEWS_API_KEY", "\"${readNewsApiKey()}\"")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}

fun readNewsApiKey(): String {
    val propsFile = rootProject.file("local.properties")
    val props = Properties().apply {
        if (propsFile.exists()) propsFile.inputStream().use { load(it) }
    }
    val fromLocal = props.getProperty("NEWS_API_KEY")?.trim().orEmpty()
    val fromEnv = System.getenv("NEWS_API_KEY")?.trim().orEmpty()
    return (fromLocal.ifEmpty { fromEnv }).ifEmpty { "REPLACE_ME" }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.compose.material.icons.extended)

    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.nav.compose)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)

    implementation(libs.biometric)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // JUnit Jupiter
    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Run JUnit4 tests (Robolectric runner) on JUnit Platform
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)

    // Robolectric + Room JVM tests
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)

    // Others
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.coroutines.test)
}