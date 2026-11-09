package com.wherever.mobile.arch

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File
import org.junit.Test

/**
 * Suite de arquitectura de la constitución §7 (AGENTS §4) con Konsist: capas, `domain` puro, `ui` sin
 * acceso a datos, ViewModels con Hilt y estado inmutable hacia fuera, despachadores inyectados y nombres de nv-04.
 *
 * Las reglas de capas vienen de `main`, adaptadas a este árbol: allí la capa `ui..` existía como paquete
 * raíz y aquí el UI vive en `feature/<n>/ui`, `core/ui` y `navigation` (docs/reconciliacion-main.md §3).
 */
class ArchitectureTest {
    private val scope = Konsist.scopeFromProduction()

    private val forbiddenInDomain = listOf("android.", "androidx.", "retrofit2.", "okhttp3.", "kotlinx.serialization.")
    private val forbiddenInUi = listOf(
        "retrofit2.",
        "okhttp3.",
        "androidx.room.",
        "com.wherever.mobile.core.network.",
        "com.wherever.mobile.core.database."
    )

    @Test
    fun `las capas solo dependen de las permitidas`() {
        // `core/database` se valida aparte: `WhereverDatabase` registra las entidades y DAOs
        // de cada feature porque Room lo exige (nv-04: "cada feature aporta entidades y DAOs").
        scope.slice { !it.isInPackage(DATABASE_PACKAGE) }.assertArchitecture {
            val core = Layer("Core", "com.wherever.mobile.core..")
            val feature = Layer("Feature", "com.wherever.mobile.feature..")
            val navigation = Layer("Navigation", "com.wherever.mobile.navigation..")

            core.dependsOnNothing()
            feature.dependsOn(core)
            navigation.dependsOn(feature, core)
        }
    }

    @Test
    fun `core database solo alcanza entidades y DAOs de las features`() {
        val databaseFiles = scope.files.filter { it.isInPackage(DATABASE_PACKAGE) }
        org.junit.Assert.assertTrue("core/database sin archivos", databaseFiles.isNotEmpty())
        databaseFiles.assertTrue { file ->
            file.imports
                .map { it.name }
                .filter { it.startsWith(FEATURE_PACKAGE) || it.startsWith(NAVIGATION_PACKAGE) }
                .all { it.contains(".data.") && (it.endsWith("Entity") || it.endsWith("Dao")) }
        }
    }

    @Test
    fun `domain no depende de Android ni de red ni de otras capas`() {
        scope.files.filter { it.packagee?.name?.contains(".domain") == true }.assertFalse { file ->
            file.imports.any { import ->
                forbiddenInDomain.any { import.name.startsWith(it) } ||
                    import.name.contains(".data.") ||
                    import.name.contains(".ui.")
            }
        }
    }

    @Test
    fun `ui no accede a red, Room ni implementaciones de datos`() {
        scope.files
            .filter { file -> file.packagee?.name.orEmpty().let { it.contains(".feature.") && it.endsWith(".ui") } }
            .assertFalse { file ->
                file.imports.any { import ->
                    forbiddenInUi.any { import.name.startsWith(it) } || import.name.contains(".data.")
                }
            }
    }

    @Test
    fun `una clase con sufijo ViewModel solo vive en ui o navigation`() {
        scope.classes().assertTrue { declaration ->
            val filePath = declaration.containingFile.path.replace('\\', '/')
            val livesInFeatureUi = filePath.contains("/feature/") && filePath.contains("/ui/")
            !declaration.name.endsWith("ViewModel") ||
                declaration.resideInPackage("com.wherever.mobile.navigation..") ||
                declaration.resideInPackage("com.wherever.mobile.core.ui..") ||
                livesInFeatureUi
        }
    }

    @Test
    fun `los ViewModels usan Hilt, viven en ui o navigation y terminan en ViewModel`() {
        val viewModels = scope.classes().filter { cls -> cls.parents().any { it.name == "ViewModel" } }
        assertFound(viewModels.isNotEmpty())
        viewModels.assertTrue { vm ->
            vm.name.endsWith("ViewModel") &&
                vm.hasAnnotationWithName("HiltViewModel") &&
                (vm.resideInPackage("..ui..") || vm.resideInPackage("..navigation.."))
        }
    }

    @Test
    fun `el estado mutable de los ViewModels es privado`() {
        scope.classes().filter { cls -> cls.parents().any { it.name == "ViewModel" } }
            .flatMap { it.properties() }
            .filter { it.text.contains("MutableStateFlow") || it.text.contains("MutableSharedFlow") }
            .assertTrue { it.hasPrivateModifier }
    }

    @Test
    fun `ningun ViewModel usa Retrofit, OkHttp, Room ni las interfaces Api`() {
        scope.files.filter { it.name.endsWith("ViewModel") }.assertFalse { file ->
            file.text.contains("retrofit2.") ||
                file.text.contains("okhttp3.") ||
                file.text.contains("androidx.room.") ||
                file.imports.any { it.name.endsWith("Api") }
        }
    }

    @Test
    fun `el codigo demo no vive en produccion`() {
        // El working dir de las pruebas de Gradle es `app/`.
        org.junit.Assert.assertFalse(File("src/main/java/com/wherever/mobile/demo").exists())
    }

    @Test
    fun `los Composables no viven en data ni domain y no reciben repositorios`() {
        scope.functions().filter { it.hasAnnotationWithName("Composable") }.assertTrue { fn ->
            !fn.resideInPackage("..data..") &&
                !fn.resideInPackage("..domain..") &&
                fn.parameters.none { it.type.name.endsWith("Repository") || it.type.name.endsWith("Api") }
        }
    }

    @Test
    fun `los Composables no usan texto visible fijo`() {
        scope.files.assertFalse { file ->
            file.text.contains("@Composable") &&
                file.text.contains(Regex("""Text\(\s*(text\s*=\s*)?"[^"]+"""))
        }
    }

    @Test
    fun `los despachadores solo se nombran en DefaultDispatcherProvider`() {
        scope.files.filter { it.nameWithExtension != "DefaultDispatcherProvider.kt" }.assertFalse { file ->
            file.text.contains("Dispatchers.IO") ||
                file.imports.any { it.name == "kotlinx.coroutines.Dispatchers" }
        }
    }

    @Test
    fun `repositorios como interfaz en domain e implementacion en data`() {
        scope.interfaces().filter { it.name.endsWith("Repository") }.assertTrue { it.resideInPackage("..domain..") }
        scope.classes().filter { it.name.endsWith("RepositoryImpl") }.assertTrue { impl ->
            impl.resideInPackage("..data..") && impl.parents().any { it.name == impl.name.removeSuffix("Impl") }
        }
    }

    @Test
    fun `sufijos de nombre de nv-04 para Room, Retrofit y DTO`() {
        val daos = scope.interfaces().filter { it.hasAnnotationWithName("Dao") }
        val entities = scope.classes().filter { it.hasAnnotationWithName("Entity") }
        val apis = scope.files.filter { file -> file.imports.any { it.name.startsWith("retrofit2.http.") } }
            .flatMap { it.interfaces() }
        val dtos = scope.classes().filter { it.name.endsWith("Dto") }
        assertFound(listOf(daos, entities, apis, dtos).all { it.isNotEmpty() })
        daos.assertTrue { it.name.endsWith("Dao") }
        entities.assertTrue { it.name.endsWith("Entity") }
        apis.assertTrue { it.name.endsWith("Api") }
        dtos.assertTrue { it.resideInPackage("..data..") || it.resideInPackage("..core.network..") }
    }

    private fun assertFound(condition: Boolean) =
        org.junit.Assert.assertTrue("la regla no encontró declaraciones", condition)

    private fun KoFileDeclaration.isInPackage(prefix: String): Boolean =
        packagee?.name?.let { it == prefix || it.startsWith("$prefix.") } == true

    private companion object {
        const val DATABASE_PACKAGE = "com.wherever.mobile.core.database"
        const val FEATURE_PACKAGE = "com.wherever.mobile.feature."
        const val NAVIGATION_PACKAGE = "com.wherever.mobile.navigation."
    }
}
