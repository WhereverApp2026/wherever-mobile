package com.wherever.mobile.arch

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File
import org.junit.Test

class ArchitectureTest {

    @Test
    fun layersOnlyDependOnAllowedLayers() {
        Konsist.scopeFromProduction().assertArchitecture {
            val core = Layer("Core", "com.wherever.mobile.core..")
            val ui = Layer("UI", "com.wherever.mobile.ui..")
            val feature = Layer("Feature", "com.wherever.mobile.feature..")
            val navigation = Layer("Navigation", "com.wherever.mobile.navigation..")

            core.dependsOnNothing()
            ui.dependsOn(core)
            feature.dependsOn(core, ui)
            navigation.dependsOn(feature, ui, core)
        }
    }

    @Test
    fun viewModelsLiveInUiPackagesAndUseTheSuffix() {
        Konsist.scopeFromProduction().classes().assertTrue { declaration ->
            val filePath = declaration.containingFile.path.replace('\\', '/')
            val livesInFeatureUi = filePath.contains("/feature/") && filePath.contains("/ui/")
            !declaration.name.endsWith("ViewModel") ||
                declaration.resideInPackage("com.wherever.mobile.ui..") ||
                declaration.resideInPackage("com.wherever.mobile.core.ui..") ||
                livesInFeatureUi
        }

        Konsist.scopeFromProduction().classes()
            .filter { it.name.endsWith("ViewModel") }
            .assertTrue { declaration -> declaration.name.endsWith("ViewModel") }
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

        val demoPath = File(
            System.getProperty("rootProjectDir"),
            "app/src/main/java/com/wherever/mobile/demo"
        )
        org.junit.Assert.assertFalse(demoPath.exists())
    }
}
