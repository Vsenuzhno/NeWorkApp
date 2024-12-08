package ru.netology.nework.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authStateFlow = MutableStateFlow(
        AuthState(
            prefs.getInt("id", 0),
            prefs.getString("token", null),
            prefs.getString("avatar", null)
        )
    )
    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Int, token: String, avatar: String? = null) {
        _authStateFlow.value = AuthState(id, token, avatar)
        with(prefs.edit()) {
            putInt("id", id)
            putString("token", token)
            putString("avatar", avatar)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState(avatar = prefs.getString("avatar", null))
        with(prefs.edit()) {
            clear()
            commit()
        }
    }
}

data class AuthState(
    val id: Int = 0,
    val token: String? = null,
    val avatar: String? = null
)