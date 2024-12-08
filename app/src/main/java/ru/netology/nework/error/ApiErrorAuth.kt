package ru.netology.nework.error

sealed interface ApiErrorAuth {
    data object Success : ApiErrorAuth
    data object IncorrectPassword : ApiErrorAuth
    data object UserNotFound : ApiErrorAuth
    data object UserRegistered : ApiErrorAuth
    data object IncorrectPhotoFormat : ApiErrorAuth
    data object UnknownError : ApiErrorAuth
}