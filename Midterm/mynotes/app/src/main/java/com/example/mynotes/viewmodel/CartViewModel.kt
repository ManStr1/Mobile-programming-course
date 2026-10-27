package com.example.mynotes.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.AppDatabase
import com.example.mynotes.data.dao.CartItemWithInfo
import com.example.mynotes.data.repo.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CartRepository(AppDatabase.get(app).cartDao())

    private val _cartId = MutableStateFlow<Long?>(null)
    val cartId = _cartId.asStateFlow()

    val items: StateFlow<List<CartItemWithInfo>> =
        cartId.flatMapLatest { id ->
            if (id == null) flow { emit(emptyList()) } else repo.items(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val total: StateFlow<Long> = items.map { list -> list.sumOf { it.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val productIds: StateFlow<Set<Long>> = items.map { list -> list.map { it.productId }.toSet()}
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun ensureCart(userId: Long?) = viewModelScope.launch {
        _cartId.value = repo.ensureCart(userId)
    }

    fun add(productId: Long) = viewModelScope.launch {
        _cartId.value?.let { repo.add(it, productId) }
    }

    fun changeQty(cartItemId: Long, delta: Int) = viewModelScope.launch {
        repo.changeQty(cartItemId, delta)
    }

    fun clear() = viewModelScope.launch {
        _cartId.value?.let { repo.clear(it) }
    }
}
