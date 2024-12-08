package ru.netology.nework.dto

import java.time.OffsetDateTime

data class Event(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: OffsetDateTime,
    val published: OffsetDateTime,
    val coords: Coords? = null,
    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    val speakerIds: List<Int>,
    val participantsIds: List<Int>,
    val participatedByMe: Boolean,
    val attachment: Attachment? = null,
    val link: String? = null,
    val users: Map<String, UserPreview>,
    val ownedByMe: Boolean = false,
) : FeedItem

enum class EventType {
    OFFLINE,
    ONLINE,
}
