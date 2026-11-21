package com.example.mynotes.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.AppDatabase
import com.example.mynotes.data.entity.UserAddress
import com.example.mynotes.data.repo.AddressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SaveResult {
    object Saved : SaveResult
    data class Invalid(val errors: Map<String, String>) : SaveResult
    data class Failed(val message: String) : SaveResult
}

class AddressViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AddressRepository(AppDatabase.get(app).addressDao())
    private val _userId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val addresses: StateFlow<List<UserAddress>> = _userId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<UserAddress>()) else repo.observe(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setUser(id: Long?) { _userId.value = id }

    suspend fun get(id: Long): UserAddress? = repo.get(id)

    fun save(
        addressId: Long?,
        street: String,
        city: String,
        region: String,
        zip: String,
        onResult: (SaveResult) -> Unit
    ) = viewModelScope.launch {
        val userId = _userId.value
        if (userId == null) {
            onResult(SaveResult.Failed("Войдите, чтобы сохранить адрес"))
            return@launch
        }
        val errors = validate(street, city, region, zip)
        if (errors.isNotEmpty()) {
            onResult(SaveResult.Invalid(errors))
            return@launch
        }
        val existing = addressId?.let { repo.get(it) }
        if (addressId != null && (existing == null || existing.userId != userId)) {
            onResult(SaveResult.Failed("Адрес не найден"))
            return@launch
        }
        repo.save(existing, userId, street.trim(), city.trim(), region.trim(), zip.trim())
        onResult(SaveResult.Saved)
    }

    fun delete(address: UserAddress, onResult: (String) -> Unit) = viewModelScope.launch {
        if (address.userId != _userId.value) {
            onResult("Нельзя удалить чужой адрес")
            return@launch
        }
        repo.delete(address)
        onResult("Адрес удалён")
    }

    private fun validate(street: String, city: String, region: String, zip: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (street.trim().length < 3) errors["street"] = "Минимум 3 символа"
        if (city.trim().isEmpty()) errors["city"] = "Введите город"
        if (region.trim().isEmpty()) errors["region"] = "Введите область"
        if (!Regex("\\d{6}").matches(zip.trim())) errors["zip"] = "Индекс — 6 цифр"
        return errors
    }
}
