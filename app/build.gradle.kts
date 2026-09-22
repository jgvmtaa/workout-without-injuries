plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Screenshot-test record/verify/compare tasks for the tests.spec suite.
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.jgv.workoutplanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jgv.workoutplanner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            // Robolectric-backed unit tests — the screenshot suite in tests.spec —
            // need merged resources and the manifest on the unit-test classpath.
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    // Pins the JDK that *compiles* Kotlin and Java, independently of whatever JVM
    // Gradle itself was launched on, and fails loudly with "no matching toolchain"
    // rather than silently compiling against a different JDK.
    //
    // What this does NOT do, despite looking like it should:
    //  - It does not choose the JVM that runs Gradle and AGP. That is JAVA_HOME's,
    //    and AGP 8.7.3 independently requires it to be 17 or newer.
    //  - It does not pin a vendor or a patch level. Any JDK 17 satisfies it.
    // `compileOptions` and `kotlinOptions` above are a third thing again: the
    // bytecode target. All three have to agree, and none of them implies the others.
    jvmToolchain(17)
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)

    // Compose (BOM keeps Compose artifacts on one aligned version)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Declared explicitly rather than relied on transitively via material3: the
    // core icon set (back arrow, info) is used by the shared core/ui components.
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Screenshot tests (tests.spec harness): Robolectric renders Compose on the
    // JVM at the pinned SDK level, Roborazzi captures and byte-compares.
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    // Instrumented tests
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
