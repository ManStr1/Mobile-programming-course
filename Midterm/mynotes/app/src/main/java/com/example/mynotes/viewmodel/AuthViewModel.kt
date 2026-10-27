package com.example.mynotes.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.AppDatabase
import com.example.mynotes.data.entity.User
import com.example.mynotes.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(AppDatabase.get(app).userDao())

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    val userId: StateFlow<Long?> = _user.map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun logout() { _user.value = null }

    fun login(username: String, password: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        if (username.isBlank() || password.isEmpty()) {
            onResult("Введите логин и пароль")
            return@launch
        }
        val u = repo.login(username.trim(), password)
        if (u == null) {
            onResult("Неверный логин или пароль")
        } else {
            _user.value = u
            onResult(null)
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        passwordRepeat: String,
        onResult: (String?) -> Unit) = viewModelScope.launch {
        val login = username.trim()
        val mail = email.trim()
        val error = when {
            login.isEmpty() -> "Введите логин"
            mail.isEmpty() -> "Введите email"
            password.isEmpty() -> "Введите пароль"
            password != passwordRepeat -> "Пароли не совпадают"
            else -> null
        }
        if (error != null) {
            onResult(error)
            return@launch
        }
        val u = repo.register(login, mail, password)
        if (u == null) {
            onResult("Такой логин уже занят")
        } else {
            onResult(null)
        }
    }
}
























