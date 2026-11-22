package com.wherever.mobile.feature.memories.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.friends.domain.Friendship
import com.wherever.mobile.feature.friends.domain.FriendshipList
import com.wherever.mobile.feature.friends.domain.FriendshipPage
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.memories.domain.MAX_TAGGED
import com.wherever.mobile.feature.memories.domain.Memory
import com.wherever.mobile.feature.memories.domain.POSTCARD_TEXT_MAX
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.Visibility
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.UploadedImage
import com.wherever.mobile.testing.FakeFriendsRepository
import com.wherever.mobile.testing.FakeMemoriesRepository
import com.wherever.mobile.testing.FakePhotoRepository
import com.wherever.mobile.testing.FakePostcardGallery
import com.wherever.mobile.testing.FakePostcardRenderer
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.samplePostcard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-31: álbum, postal (editar y ver) y Nuevo momento. */
@OptIn(ExperimentalCoroutinesApi::class)
class MemoriesViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeMemoriesRepository()

    // --- MemoriesViewModel (73, 74, 76) ---

    @Test
    fun `album pagina con el cursor de la pestana visible`() = runTest {
        repository.pages[null] = Result.Success(PageResult(listOf(samplePostcard("c1")), "n2"))
        repository.pages["n2"] = Result.Success(PageResult(listOf(samplePostcard("c2")), null))
        repository.memoriesResult = Result.Success(
            PageResult(listOf(Memory("m1", "p1", "Paseo", "2026-10-11", emptyList(), listOf("c1"))), null)
        )
        val viewModel = MemoriesViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("c1", "c2"), viewModel.state.value.postcards.map { it.id })
        assertNull(viewModel.state.value.postcardsCursor)

        viewModel.onTab(AlbumTab.BY_PLAN)
        viewModel.loadMore()
        advanceUntilIdle()
        assertFalse(repository.calls.any { it.startsWith("memories:") && !it.endsWith("null") })
        assertEquals(1, viewModel.state.value.memories.size)
    }

    @Test
    fun `sin postales ni recuerdos es 76_RecVacio`() = runTest {
        val viewModel = MemoriesViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.empty)
    }

    @Test
    fun `error y Reintentar`() = runTest {
        repository.pages[null] = Result.Failure(AppError.Network)
        val viewModel = MemoriesViewModel(repository)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.empty)

        repository.pages[null] = Result.Success(PageResult(listOf(samplePostcard()), null))
        viewModel.load()
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.postcards.size)
    }

    @Test
    fun `al volver al album recarga lo nuevo sin esqueleto`() = runTest {
        val viewModel = MemoriesViewModel(repository)
        advanceUntilIdle()
        viewModel.onResume()
        advanceUntilIdle()
        assertEquals(1, repository.calls.count { it == "postcards:null" })

        // Probado en el Xiaomi: tras guardar una postal el álbum seguía vacío hasta salir y entrar.
        repository.pages[null] = Result.Success(PageResult(listOf(samplePostcard("nueva")), null))
        viewModel.onResume()
        assertFalse(viewModel.state.value.loading)
        advanceUntilIdle()
        assertEquals(listOf("nueva"), viewModel.state.value.postcards.map { it.id })
    }

    // --- PostcardEditorViewModel (72) ---

    private val photos = FakePhotoRepository(Result.Success(UploadedImage("i1", ImageState.PUBLISHED, "u", "t")))
    private val renderer = FakePostcardRenderer()

    private fun editor() = PostcardEditorViewModel(
        repository,
        renderer,
        photos,
        SavedStateHandle(
            mapOf("source" to "content://foto", "placeId" to "n1", "placeName" to "Café Raíz", "planId" to "p1")
        )
    )

    @Test
    fun `estilo y texto de hasta 80`() = runTest {
        val viewModel = editor()
        viewModel.onStyle(PostcardStyle.PASSPORT)
        viewModel.onText("a".repeat(POSTCARD_TEXT_MAX + 5))
        assertEquals(PostcardStyle.PASSPORT, viewModel.state.value.style)
        assertEquals(POSTCARD_TEXT_MAX, viewModel.state.value.text.length)
    }

    @Test
    fun `guardar dibuja, sube y crea la postal privada`() = runTest {
        val viewModel = editor()
        viewModel.onStyle(PostcardStyle.POLAROID)
        viewModel.saved.test {
            viewModel.save()
            advanceUntilIdle()
            assertEquals(PostcardSaved.ToAlbum, awaitItem())
        }
        val created = repository.created.single()
        assertEquals("i1", created.imageId)
        assertEquals(PostcardStyle.POLAROID, created.style)
        assertEquals(Visibility.PRIVATE, created.visibility)
        assertEquals("n1", created.placeId)
        assertEquals(listOf(PostcardStyle.POLAROID), renderer.styles)
        assertEquals(listOf("file:///cache/fotos/postal.jpg"), photos.sources)
    }

    @Test
    fun `compartir crea una sola vez y entrega la imagen dibujada`() = runTest {
        val viewModel = editor()
        viewModel.saved.test {
            viewModel.share()
            advanceUntilIdle()
            assertEquals(PostcardSaved.Share("file:///cache/fotos/postal.jpg"), awaitItem())
            viewModel.share()
            advanceUntilIdle()
            awaitItem()
        }
        assertEquals(1, repository.created.size)
        assertEquals(1, photos.sources.size)
    }

    @Test
    fun `si la API no crea la postal se borra la imagen subida`() = runTest {
        repository.createResult = Result.Failure(AppError.Validation(apiMessage = "texto rechazado"))
        val viewModel = editor()
        viewModel.save()
        advanceUntilIdle()
        assertTrue("deleteImage:i1" in repository.calls)
        assertEquals(UiText.Plain("Texto rechazado"), viewModel.state.value.error)
        assertFalse(viewModel.state.value.saving)
    }

    @Test
    fun `texto rechazado por moderacion se muestra bajo el campo`() = runTest {
        repository.createResult = Result.Failure(
            AppError.Validation(
                apiMessage = "datos inválidos",
                fields = listOf(FieldError("texto", "el texto no cumple las normas de la comunidad"))
            )
        )
        val viewModel = editor()
        viewModel.onText("Polaroid QA")
        viewModel.save()
        advanceUntilIdle()
        assertEquals(UiText.Plain("El texto no cumple las normas de la comunidad"), viewModel.state.value.textError)
        assertNull(viewModel.state.value.error)

        viewModel.onText("Otro texto")
        assertNull(viewModel.state.value.textError)
    }

    @Test
    fun `foto rechazada por moderacion no crea postal`() = runTest {
        photos.result = Result.Success(UploadedImage("i9", ImageState.REJECTED, null, null))
        val viewModel = editor()
        viewModel.save()
        advanceUntilIdle()
        assertTrue(repository.created.isEmpty())
        assertEquals(UiText.Res(R.string.memories_photo_rejected), viewModel.state.value.error)
    }

    // --- PostcardDetailViewModel (75) ---

    private val gallery = FakePostcardGallery()

    private fun detail() = PostcardDetailViewModel(repository, gallery, SavedStateHandle(mapOf("id" to "c1")))

    @Test
    fun `eliminar pide confirmacion y manda la version`() = runTest {
        repository.postcardResult = Result.Success(
            com.wherever.mobile.feature.memories.domain.PostcardView(samplePostcard(version = 4), null, null)
        )
        val viewModel = detail()
        advanceUntilIdle()
        viewModel.askDelete()
        assertTrue(viewModel.state.value.confirmDelete)
        assertFalse(repository.calls.any { it.startsWith("delete:") })

        viewModel.deleted.test {
            viewModel.confirmDelete()
            advanceUntilIdle()
            awaitItem()
        }
        assertTrue("delete:c1:v4" in repository.calls)
    }

    @Test
    fun `postal ajena o borrada es 404 sin Reintentar`() = runTest {
        repository.postcardResult = Result.Failure(AppError.NotFound())
        val viewModel = detail()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.gone)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `descargar guarda la imagen y en Android viejo sugiere Compartir`() = runTest {
        val viewModel = detail()
        advanceUntilIdle()
        viewModel.download()
        advanceUntilIdle()
        assertEquals(listOf("https://img/c1.jpg"), gallery.saved)
        assertEquals(UiText.Res(R.string.memories_downloaded), viewModel.state.value.message)

        gallery.result = Result.Failure(AppError.Forbidden())
        viewModel.download()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.memories_download_use_share), viewModel.state.value.message)
    }

    // --- CreateMomentViewModel (70) ---

    private val friends = FakeFriendsRepository()

    private fun moment(): CreateMomentViewModel {
        repository.pages[null] = Result.Success(
            PageResult(listOf(samplePostcard("c1"), samplePostcard("c2", PostcardState.IN_MODERATION)), null)
        )
        friends.lists[FriendshipList.FRIENDS] = Result.Success(
            FriendshipPage(
                (1..MAX_TAGGED + 1).map { Friendship("f$it", Person("u$it", "Amigo $it", null, null), false, true) },
                0
            )
        )
        return CreateMomentViewModel(repository, friends, SavedStateHandle())
    }

    @Test
    fun `solo ofrece postales listas y elige la primera`() = runTest {
        val viewModel = moment()
        advanceUntilIdle()
        assertEquals(listOf("c1"), viewModel.state.value.postcards.map { it.id })
        assertEquals("c1", viewModel.state.value.selected?.id)
    }

    @Test
    fun `etiquetados hasta 10`() = runTest {
        val viewModel = moment()
        advanceUntilIdle()
        (1..MAX_TAGGED + 1).forEach { viewModel.onToggleFriend("u$it") }
        assertEquals(MAX_TAGGED, viewModel.state.value.tagged.size)
        assertEquals(UiText.Res(R.string.memories_tag_limit, listOf(MAX_TAGGED)), viewModel.state.value.error)

        viewModel.onToggleFriend("u1")
        assertEquals(MAX_TAGGED - 1, viewModel.state.value.tagged.size)
    }

    @Test
    fun `visibilidad y doble toque en Publicar mandan un solo momento`() = runTest {
        val viewModel = moment()
        advanceUntilIdle()
        viewModel.onVisibility(Visibility.PUBLIC)
        viewModel.onToggleFriend("u2")
        viewModel.published.test {
            viewModel.publish()
            viewModel.publish()
            advanceUntilIdle()
            awaitItem()
        }
        val draft = repository.moments.single()
        assertEquals("c1", draft.postcardId)
        assertEquals(Visibility.PUBLIC, draft.visibility)
        assertEquals(listOf("u2"), draft.tagged)
    }
}
