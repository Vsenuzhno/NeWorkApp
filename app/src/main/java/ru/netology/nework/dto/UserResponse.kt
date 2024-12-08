package ru.netology.nework.dto

data class UserResponse(
    override val id: Int,
    val login: String,
    val name: String,
    val avatar: String? = null,
    val selected: Boolean = false
) : FeedItem
