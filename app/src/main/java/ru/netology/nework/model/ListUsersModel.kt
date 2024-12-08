package ru.netology.nework.model

import ru.netology.nework.dto.UserResponse

data class ListUsersModel(
    val likers: List<UserResponse> = emptyList(),
    val participant: List<UserResponse> = emptyList(),
    val mentioned: List<UserResponse> = emptyList(),
    val speakers: List<UserResponse> = emptyList(),
)

enum class ListUsersType {
    LIKERS,
    PARTICIPANT,
    MENTIONED,
    SPEAKERS
}
