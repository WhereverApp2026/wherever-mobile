package com.wherever.mobile.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Reglas de capas con Konsist (constitución §4, `AGENTS.md` §3 y §5): `domain` puro, `ui` sin acceso a datos,
 * ViewModels con Hilt y estado inmutable hacia fuera, despachadores inyectados.
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
    fun `los ViewModels usan Hilt, viven en ui o navigation y terminan en ViewModel`() {
        val viewModels = scope.classes().filter { cls -> cls.parents().any { it.name == "ViewModel" } }
        assertTrue(viewModels.isNotEmpty())
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
    fun `los Composables no viven en data ni domain y no reciben repositorios`() {
        scope.functions().filter { it.hasAnnotationWithName("Composable") }.assertTrue { fn ->
            !fn.resideInPackage("..data..") &&
                !fn.resideInPackage("..domain..") &&
                fn.parameters.none { it.type.name.endsWith("Repository") || it.type.name.endsWith("Api") }
        }
    }

    @Test
    fun `los despachadores solo se crean en core di`() {
        scope.files.filter { it.packagee?.name != "com.wherever.mobile.core.di" }.assertFalse { file ->
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
    fun `ningun ViewModel usa Retrofit, OkHttp ni las interfaces Api`() {
        scope.files.filter { it.name.endsWith("ViewModel") }.assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("retrofit2.") ||
                    import.name.startsWith("okhttp3.") ||
                    import.name.endsWith("Api")
            }
        }
    }

    @Test
    fun `sufijos de nombre de nv-04 para Room, Retrofit y DTO`() {
        val daos = scope.interfaces().filter { it.hasAnnotationWithName("Dao") }
        val entities = scope.classes().filter { it.hasAnnotationWithName("Entity") }
        val apis = scope.files.filter { file -> file.imports.any { it.name.startsWith("retrofit2.http.") } }
            .flatMap { it.interfaces() }
        val dtos = scope.classes().filter { it.name.endsWith("Dto") }
        assertTrue(listOf(daos, entities, apis, dtos).all { it.isNotEmpty() })
        daos.assertTrue { it.name.endsWith("Dao") }
        entities.assertTrue { it.name.endsWith("Entity") }
        apis.assertTrue { it.name.endsWith("Api") }
        dtos.assertTrue { it.resideInPackage("..data..") || it.resideInPackage("..core.network..") }
    }

    private fun assertTrue(condition: Boolean) =
        org.junit.Assert.assertTrue("la regla no encontró declaraciones", condition)
}
