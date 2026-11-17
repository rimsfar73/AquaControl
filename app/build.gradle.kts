
plugins {
    alias(aclibs.plugins.android.application)
    alias(aclibs.plugins.kotlin.compose)
    alias(aclibs.plugins.ksp)
}

// URL configurable para cada desarrollador.
val backendUrl = providers.gradleProperty("AQUA_BACKEND_URL")

android {
    namespace = "com.example.aquacontrol"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.aquacontrol"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            // Cobertura de pruebas unitarias.
            enableUnitTestCoverage = true

            // Emulador Android -> backend del PC.
            val url = backendUrl.getOrElse(
                "http://10.0.2.2:8080/"
            )

            buildConfigField(
                "String",
                "BACKEND_URL",
                "\"$url\""
            )
        }

        getByName("release") {
            // URL configurada al compilar la aplicación.
            val url = backendUrl.getOrElse("")

            buildConfigField(
                "String",
                "BACKEND_URL",
                "\"$url\""
            )

            isMinifyEnabled = false
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
}

dependencies {
    // Jetpack Compose
    implementation(platform(aclibs.androidx.compose.bom))
    implementation(aclibs.androidx.compose.ui)
    implementation(aclibs.androidx.compose.ui.graphics)
    implementation(aclibs.androidx.compose.ui.tooling.preview)
    implementation(aclibs.androidx.compose.material3)

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    // Android y navegación
    implementation(aclibs.androidx.activity.compose)
    implementation(aclibs.androidx.core.ktx)
    implementation(aclibs.androidx.lifecycle.runtime.ktx)

    implementation(
        "androidx.navigation:navigation-compose:2.8.0"
    )

    // Imágenes
    implementation(
        "io.coil-kt.coil3:coil-compose:3.2.0"
    )

    // Room + KSP
    implementation(aclibs.room.runtime)
    implementation(aclibs.room.ktx)
    ksp(aclibs.room.compiler)

    // Pruebas unitarias
    testImplementation(aclibs.junit)

    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"
    )

    testImplementation(
        "com.squareup.okhttp3:okhttp:3.14.9"
    )

    // Pruebas instrumentadas
    androidTestImplementation(
        platform(aclibs.androidx.compose.bom)
    )
    androidTestImplementation(
        aclibs.androidx.compose.ui.test.junit4
    )
    androidTestImplementation(aclibs.androidx.espresso.core)
    androidTestImplementation(aclibs.androidx.junit)

    // Herramientas Compose
    debugImplementation(aclibs.androidx.compose.ui.tooling)
    debugImplementation(aclibs.androidx.compose.ui.test.manifest)

    // Retrofit
    implementation(
        "com.squareup.retrofit2:retrofit:2.11.0"
    )
    implementation(
        "com.squareup.retrofit2:converter-gson:2.11.0"
    )
}
