package com.example.mynotes.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.AppDatabase
import com.example.mynotes.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(AppDatabase.get(app).userDao())

    private val _userId = MutableStateFlow<Long?>(null)
    val userId = _userId.asStateFlow()

    fun logout() { _userId.value = null }

    fun login(username: String, password: String, onResult: (Boolean)->Unit) = viewModelScope.launch {
        val u = repo.login(username, password)
        _userId.value = u?.userId
        onResult(u != null)
    }

    fun register(username: String, email: String, password: String, onResult: (Boolean)->Unit) = viewModelScope.launch {
        val id = repo.register(username, email, password)
        _userId.value = id
        onResult(true)
    }
}
