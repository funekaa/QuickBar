import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// An unsigned APK cannot be installed at all: Android's package parser fails and the
// installer reports "packageinfo is null". Credentials come from keystore.properties
// (git-ignored). When that file is absent the release build falls back to the debug
// signing identity, so assembleRelease always produces something installable.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { stream -> load(stream) }
    }
}

val appVersionName = "1.0.2"
val appVersionCode = 3

android {
    namespace = "com.picdraw.quickbar"
    compileSdk = 35
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.picdraw.quickbar"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        resourceConfigurations += listOf("en", "zh-rCN")
    }

    signingConfigs {
        if (keystoreProperties.getProperty("storeFile") != null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // material-icons-extended ships ~2000 icons while the app only references 100,
            // so shrinking is what keeps the release APK reasonable.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
}

// AGP 8 removed the only API that could rename an APK, so the artifact is renamed once the
// build is done: QuickBar-1.0.0-debug.apk / QuickBar-1.0.0-release.apk.
// output-metadata.json is updated too, otherwise IDE and CLI installers would look for the
// old file name.
val versionApkNames by tasks.registering {
    group = "build"
    description = "Renames each built APK to include the app version."
    dependsOn("assemble")

    doLast {
        val apkRoot = layout.buildDirectory.dir("outputs/apk").get().asFile
        apkRoot.listFiles().orEmpty().filter { it.isDirectory }.forEach { variantDir ->
            val newStem = "QuickBar-$appVersionName-${variantDir.name}"

            val apks = variantDir.listFiles().orEmpty().filter { it.name.endsWith(".apk") }
            apks.forEach apkLoop@{ apk ->
                val oldStem = apk.name.removeSuffix(".apk")
                val target = File(variantDir, "$newStem.apk")
                if (apk.name == target.name) return@apkLoop
                if (!apk.renameTo(target)) {
                    logger.warn("Could not rename ${apk.name} to ${target.name}")
                    return@apkLoop
                }
                // The ART baseline profile files carry the APK stem too, so they move with it.
                File(variantDir, "baselineProfiles").walkTopDown()
                    .filter { it.isFile && it.nameWithoutExtension == oldStem }
                    .forEach { profile ->
                        val moved = File(profile.parentFile, "$newStem.dm")
                        if (!profile.renameTo(moved)) {
                            logger.warn("Could not rename ${profile.name} to ${moved.name}")
                        }
                    }
            }

            // output-metadata.json is what IDEs and the CLI read to find the artifact, so the
            // old stem has to go. The baseline profile paths embed it as well.
            val metadata = File(variantDir, "output-metadata.json")
            if (metadata.exists()) {
                var text = metadata.readText()
                Regex("""[\w.\-]+\.apk""").findAll(text).map { it.value }.distinct().forEach { oldName ->
                    text = text.replace(oldName.removeSuffix(".apk"), newStem)
                }
                metadata.writeText(text)
            }
        }
    }
}
