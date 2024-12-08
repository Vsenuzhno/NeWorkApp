package ru.netology.nework.dto

import java.time.OffsetDateTime

data class Post(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val published: OffsetDateTime,
    val coords: Coords? = null,
    val link: String? = null,
    val mentionIds: List<Int>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    val attachment: Attachment? = null,
    val users: Map<String, UserPreview>,
    val ownedByMe: Boolean = false,
) : FeedItem