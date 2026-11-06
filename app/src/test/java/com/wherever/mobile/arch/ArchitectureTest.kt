package com.wherever.mobile.arch

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File
import org.junit.Test

/**
 * Suite de arquitectura de la constitución §7 (AGENTS §4), en portable.
 *
 * Portada de `main` y adaptada a este árbol: allí la capa `ui..` existía como paquete raíz y
 * aquí el UI vive en `feature/<n>/ui`, `core/ui` y `navigation` (docs/reconciliacion-main.md §3).
 */
class ArchitectureTest {

    @Test
    fun layersOnlyDependOnAllowedLayers() {
        Konsist.scopeFromProduction().assertArchitecture {
            val core = Layer("Core", "com.wherever.mobile.core..")
            val feature = Layer("Feature", "com.wherever.mobile.feature..")
            val navigation = Layer("Navigation", "com.wherever.mobile.navigation..")

            core.dependsOnNothing()
            feature.dependsOn(core)
            navigation.dependsOn(feature, core)
        }
    }

    @Test
    fun viewModelsLiveInUiPackagesAndUseTheSuffix() {
        Konsist.scopeFromProduction().classes().assertTrue { declaration ->
            val filePath = declaration.containingFile.path.replace('\\', '/')
            val livesInFeatureUi = filePath.contains("/feature/") && filePath.contains("/ui/")
            !declaration.name.endsWith("ViewModel") ||
                declaration.resideInPackage("com.wherever.mobile.navigation..") ||
                declaration.resideInPackage("com.wherever.mobile.core.ui..") ||
                livesInFeatureUi
        }
    }

    @Test
    fun dispatchersAreNotHardcodedOutsideTheDefaultProvider() {
        Konsist.scopeFromProduction().files.assertFalse { file ->
            !file.nameWithExtension.endsWith("DispatcherProvider.kt") &&
                file.text.contains("Dispatchers.IO")
        }
    }

    @Test
    fun composablesDoNotUseFixedVisibleText() {
        Konsist.scopeFromProduction().files.assertFalse { file ->
            file.text.contains("@Composable") &&
                file.text.contains(Regex("""Text\(\s*(text\s*=\s*)?"[^"]+"""))
        }
    }

    @Test
    fun viewModelsDoNotReachNetworkOrDatabaseAndDemoStaysInTests() {
        Konsist.scopeFromProduction().files.assertFalse { file ->
            file.nameWithExtension.endsWith("ViewModel.kt") &&
                (
                    file.text.contains("retrofit2.") ||
                        file.text.contains("androidx.room.")
                    )
        }

        // El working dir de las pruebas de Gradle es `app/`.
        val demoPath = File("src/main/java/com/wherever/mobile/demo")
        org.junit.Assert.assertFalse(demoPath.exists())
    }
}
