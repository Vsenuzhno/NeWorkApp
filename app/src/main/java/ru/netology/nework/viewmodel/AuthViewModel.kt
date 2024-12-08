package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Media
import ru.netology.nework.error.ApiErrorAuth
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.repository.Repository
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: Repository
) : ViewModel() {

    val dataAuth: LiveData<AuthState> = appAuth.authStateFlow
        .asLiveData(Dispatchers.Default)

    private val _media = MutableLiveData<Media>()
    val media: LiveData<Media> = _media

    val authenticated: Boolean
        get() = appAuth.authStateFlow.value.id != 0

    suspend fun registration(login: String, name: String, pass: String): ApiErrorAuth {
        val attachment = _media.value?.let { media ->
            AttachmentModel(
                attachmentType = AttachmentType.IMAGE,
                uri = Uri.parse(media.url),
                file = File(media.url)
            )
        }
        try {
            return repository.registration(login, name, pass, attachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun login(login: String, pass: String): ApiErrorAuth {
        try {
            return repository.login(login, pass)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    fun logout() {
        repository.logout()
    }

    fun mediaSaveMedia(file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = repository.mediaSaveMedia(file)
                if (response.isSuccessful) {
                    response.body()?.let { media ->
                        _media.value = media
                    }
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}