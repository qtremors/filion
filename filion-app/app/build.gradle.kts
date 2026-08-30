import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("filion.android.application.conventions")
}

android {
    namespace = "dev.qtremors.filion"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.qtremors.filion"
        minSdk = 30
        targetSdk = 37
        versionCode = 7
        versionName = "0.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreProperties = Properties()
    var keystorePropertiesFile = rootProject.file("signing.properties")
    if (!keystorePropertiesFile.exists()) {
        keystorePropertiesFile = rootProject.file("local.properties")
    }
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(keystoreProperties::load)
    }

    val storeFileProp = keystoreProperties["signing.storeFile"]?.toString()
    val storePasswordProp = keystoreProperties["signing.storePassword"]?.toString()
    val keyAliasProp = keystoreProperties["signing.keyAlias"]?.toString()
    val keyPasswordProp = keystoreProperties["signing.keyPassword"]?.toString()
    val hasSigningConfig = listOf(
        storeFileProp,
        storePasswordProp,
        keyAliasProp,
        keyPasswordProp
    ).all { it != null }

    if (hasSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile = file(storeFileProp!!)
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appLabel"] = "Filion Debug"
            enableUnitTestCoverage = true
        }
        release {
            manifestPlaceholders["appLabel"] = "Filion"
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            keepDebugSymbols += setOf(
                "**/libandroidx.graphics.path.so",
                "**/libfilament-jni.so",
                "**/libgltfio-jni.so",
                "**/libfilament-utils-jni.so"
            )
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

androidComponents {
    onVariants { variant ->
        val baseVersion = variant.outputs.firstOrNull()?.versionName?.get() ?: "0.0.0"
        variant.outputs.forEach { output ->
            val abiFilter = output.filters.find {
                it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI
            }?.identifier
            val name = if (abiFilter != null) {
                "Filion-$baseVersion-$abiFilter.apk"
            } else {
                "Filion-$baseVersion.apk"
            }
            output.outputFileName.set(name)
        }
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")
    metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.filament.android)
    implementation(libs.filament.gltfio.android)
    implementation(libs.filament.utils.android)
    implementation(libs.sceneview)

    testImplementation(libs.junit)
    testImplementation(libs.org.robolectric.robolectric)
}
