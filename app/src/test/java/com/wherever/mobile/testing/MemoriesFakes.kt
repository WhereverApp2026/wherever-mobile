package com.wherever.mobile.testing

import com.wherever.mobile.core.common.PageResult
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.feature.memories.domain.MemoriesRepository
import com.wherever.mobile.feature.memories.domain.Memory
import com.wherever.mobile.feature.memories.domain.MomentDraft
import com.wherever.mobile.feature.memories.domain.NewPostcard
import com.wherever.mobile.feature.memories.domain.Postcard
import com.wherever.mobile.feature.memories.domain.PostcardGallery
import com.wherever.mobile.feature.memories.domain.PostcardRenderer
import com.wherever.mobile.feature.memories.domain.PostcardState
import com.wherever.mobile.feature.memories.domain.PostcardStyle
import com.wherever.mobile.feature.memories.domain.PostcardView
import com.wherever.mobile.feature.memories.domain.Visibility

fun samplePostcard(id: String = "c1", state: PostcardState = PostcardState.READY, version: Long = 1) = Postcard(
    id = id,
    imageUrl = if (state == PostcardState.READY) "https://img/$id.jpg" else null,
    style = PostcardStyle.CLASSIC,
    text = "Saludos",
    visibility = Visibility.PRIVATE,
    state = state,
    planId = null,
    placeId = null,
    version = version,
    createdAt = "2026-10-11T18:00:00Z"
)

/** Recuerdos en memoria: anota las llamadas y responde lo que diga cada prueba. */
class FakeMemoriesRepository : MemoriesRepository {
    val calls = mutableListOf<String>()
    val pages = mutableMapOf<String?, Result<PageResult<Postcard>>>()
    var memoriesResult: Result<PageResult<Memory>> = Result.Success(PageResult(emptyList(), null))
    var postcardResult: Result<PostcardView> = Result.Success(PostcardView(samplePostcard(), null, null))
    var createResult: Result<String> = Result.Success("nueva")
    var deleteResult: Result<Unit> = Result.Success(Unit)
    var momentResult: Result<Unit> = Result.Success(Unit)
    val created = mutableListOf<NewPostcard>()
    val moments = mutableListOf<MomentDraft>()

    override suspend fun postcards(cursor: String?): Result<PageResult<Postcard>> {
        calls += "postcards:$cursor"
        return pages[cursor] ?: Result.Success(PageResult(emptyList(), null))
    }

    override suspend fun memories(cursor: String?) = memoriesResult.also { calls += "memories:$cursor" }

    override suspend fun postcard(id: String) = postcardResult.also { calls += "postcard:$id" }

    override suspend fun createPostcard(postcard: NewPostcard): Result<String> {
        calls += "create"
        created += postcard
        return createResult
    }

    override suspend fun deletePostcard(postcard: Postcard) =
        deleteResult.also { calls += "delete:${postcard.id}:v${postcard.version}" }

    override suspend fun deleteImage(imageId: String) = Result.Success(Unit).also { calls += "deleteImage:$imageId" }

    override suspend fun createMoment(draft: MomentDraft): Result<Unit> {
        calls += "moment"
        moments += draft
        return momentResult
    }
}

class FakePostcardRenderer(var result: Result<String> = Result.Success("file:///cache/fotos/postal.jpg")) :
    PostcardRenderer {
    val styles = mutableListOf<PostcardStyle>()

    override suspend fun render(source: String, style: PostcardStyle, title: String, subtitle: String): Result<String> {
        styles += style
        return result
    }
}

class FakePostcardGallery(var result: Result<Unit> = Result.Success(Unit)) : PostcardGallery {
    val saved = mutableListOf<String>()

    override suspend fun save(imageUrl: String): Result<Unit> = result.also { saved += imageUrl }
}
