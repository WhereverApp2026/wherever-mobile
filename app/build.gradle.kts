import io.gitlab.arturbosch.detekt.Detekt
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}
val configuredApiBaseUrl: String? = localProperties.getProperty("API_BASE_URL")
val stagingApiBaseUrl: String = providers.gradleProperty("wherever.stagingApiBaseUrl").get()

/** Falla al configurar si la URL rompe la regla de solo HTTPS (constitución §6) o la de Retrofit (barra final). */
fun checkedApiBaseUrl(url: String): String {
    require(url.startsWith("https://") && url.endsWith("/")) {
        "API_BASE_URL debe usar https:// y terminar en /: $url"
    }
    return url
}

android {
    namespace = "com.wherever.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wherever.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TERMS_VERSION", "\"2026-10\"")
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${checkedApiBaseUrl(configuredApiBaseUrl ?: stagingApiBaseUrl)}\""
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${checkedApiBaseUrl(configuredApiBaseUrl ?: "https://example.invalid/api/v1/")}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        // Avisos de "hay versión nueva": dependen de la fecha, no del código. Las actualiza Dependabot.
        // OldTargetApi: targetSdk 36 es decisión del equipo hasta tener SDK 37 en todos los equipos (AGENTS.md §5).
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom("src/main/java", "src/test/java", "src/androidTest/java")
}

// detekt 1.23 toma por defecto la versión del JDK de Gradle y no acepta las mayores a 22.
// Límite conocido: además, no corre sobre JDK 25; usar JDK 17-22 para esta tarea (ver Memoria.md).
tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Cobertura (constitución §7, specs/nv-39 §3.3): `koverVerifyDebug` falla bajo el 45 % de líneas.
kover {
    reports {
        filters {
            excludes {
                // Código generado por Hilt, Room, Compose y AGP: no lo escribimos nosotros.
                classes(
                    "*_Factory",
                    "*_Factory\$*",
                    "*_MembersInjector",
                    "*_HiltModules*",
                    "*_GeneratedInjector",
                    "Hilt_*",
                    "*.Hilt_*",
                    "dagger.hilt.*",
                    "hilt_aggregated_deps.*",
                    "*_Impl",
                    "*_Impl\$*",
                    "*.BuildConfig",
                    "*ComposableSingletons*"
                )
                // Las pantallas Compose se prueban con Compose Testing en `connectedDebugAndroidTest`, no en la JVM.
                annotatedBy("androidx.compose.runtime.Composable", "androidx.compose.ui.tooling.preview.Preview")
            }
        }
        verify {
            rule("Cobertura de líneas (constitución §7)") {
                minBound(45)
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.okhttp.tls)
}
