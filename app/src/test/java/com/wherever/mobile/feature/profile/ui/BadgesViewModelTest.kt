package com.wherever.mobile.feature.profile.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.profile.domain.ApiLevel
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.BadgesRepository
import com.wherever.mobile.feature.profile.domain.LevelProgress
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BadgesViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val profiles = FakeProfileRepository(sampleProfile())

    private val apiBadges = object : BadgesRepository {
        override suspend fun badges(): Result<List<Badge>> = Result.Success(
            listOf(
                Badge("resenista", "Reseñista", "Publica tu primera reseña", earned = true, current = 1, goal = 1),
                Badge("catador_cafe", "Catador de café", "5 cafeterías", earned = false, current = 2, goal = 5),
                Badge("anfitrion_grupo", "Anfitrión de grupo", "Organiza un grupo", earned = false, goal = 1)
            )
        )
    }

    @Test
    fun `separa ganadas y por desbloquear y muestra el nivel del perfil`() = runTest {
        val viewModel = BadgesViewModel(profiles, apiBadges)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals(listOf("resenista"), state.earned.map { it.id })
        assertEquals(listOf("catador_cafe", "anfitrion_grupo"), state.locked.map { it.id })
        assertEquals(0.4f, state.locked.first().fraction)
        assertTrue(state.locked.none { it.earned })
        assertEquals(sampleProfile().points, state.progress?.points)
        assertNull(state.error)
    }

    @Test
    fun `con el nivel de la API muestra su numero y la meta del siguiente`() = runTest {
        val withLevel = object : BadgesRepository {
            override suspend fun badges(): Result<List<Badge>> = Result.Success(emptyList())

            override fun lastLevel(): ApiLevel = ApiLevel(3, "Explorador", 340, "Anfitrión", 500)
        }
        val viewModel = BadgesViewModel(profiles, withLevel)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(3, state.levelNumber)
        assertEquals("Explorador", state.progress?.current?.name)
        assertEquals("Anfitrión", state.progress?.next?.name)
        assertEquals(0.68f, state.progress?.fraction)
    }

    @Test
    fun `si el siguiente nivel se llama igual guarda su numero`() = runTest {
        val sameName = object : BadgesRepository {
            override suspend fun badges(): Result<List<Badge>> = Result.Success(emptyList())

            override fun lastLevel(): ApiLevel = ApiLevel(1, "Explorador", 85, "Explorador", 150, nextNumber = 2)
        }
        val viewModel = BadgesViewModel(profiles, sameName)
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.nextLevelNumber)
        assertEquals("Explorador", viewModel.state.value.progress?.next?.name)
    }

    @Test
    fun `sin red conserva el nivel guardado y avisa`() = runTest {
        profiles.refreshResult = Result.Failure(AppError.Network)
        val viewModel = BadgesViewModel(profiles, apiBadges)
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
        assertEquals(sampleProfile().points, viewModel.state.value.progress?.points)
    }

    @Test
    fun `si fallan las insignias muestra el error sin listas`() = runTest {
        val failing = object : BadgesRepository {
            override suspend fun badges(): Result<List<Badge>> = Result.Failure(AppError.Network)
        }
        val viewModel = BadgesViewModel(profiles, failing)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.loading)
        assertTrue(viewModel.state.value.earned.isEmpty())
        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
    }

    @Test
    fun `progreso hacia el siguiente nivel`() {
        val progress = LevelProgress.of(points = 50, levelName = "Explorador")
        assertEquals("Aventurero", progress.next?.name)
        assertEquals(200, progress.pointsToNext)
        assertEquals(0.2f, progress.fraction, 0.001f)
    }

    @Test
    fun `nivel por puntos si la API trae uno desconocido`() {
        val progress = LevelProgress.of(points = 800, levelName = "Otro")
        assertEquals("Trotamundos", progress.current.name)
    }

    @Test
    fun `el ultimo nivel no tiene siguiente y queda lleno`() {
        val progress = LevelProgress.of(points = 5000, levelName = "Leyenda")
        assertNull(progress.next)
        assertEquals(0, progress.pointsToNext)
        assertEquals(1f, progress.fraction, 0.001f)
    }
}
