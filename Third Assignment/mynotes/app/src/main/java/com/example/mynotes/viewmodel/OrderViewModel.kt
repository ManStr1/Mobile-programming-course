package com.example.mynotes.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.AppDatabase
import com.example.mynotes.data.entity.Order
import com.example.mynotes.data.repo.OrderDetails
import com.example.mynotes.data.repo.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = OrderRepository(AppDatabase.get(app).orderDao())

    private val _userId = MutableStateFlow<Long?>(null)
    private val _orderId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val orders: StateFlow<List<Order>> = _userId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Order>()) else repo.orders(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val details: StateFlow<OrderDetails?> = _orderId
        .flatMapLatest { id -> if (id == null) flowOf<OrderDetails?>(null) else repo.details(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setUser(id: Long?) { _userId.value = id }

    fun selectOrder(id: Long?) { _orderId.value = id }

    fun pay(orderId: Long, onResult: (String) -> Unit) = viewModelScope.launch {
        onResult(if (repo.pay(orderId)) "Заказ оплачен" else UNAVAILABLE)
    }

    fun ship(orderId: Long, onResult: (String) -> Unit) = viewModelScope.launch {
        onResult(if (repo.ship(orderId)) "Заказ отправлен" else UNAVAILABLE)
    }

    fun deliver(orderId: Long, onResult: (String) -> Unit) = viewModelScope.launch {
        onResult(if (repo.deliver(orderId)) "Получение подтверждено" else UNAVAILABLE)
    }

    fun cancel(orderId: Long, onResult: (String) -> Unit) = viewModelScope.launch {
        onResult(if (repo.cancel(orderId)) "Заказ отменён" else UNAVAILABLE)
    }

    companion object {
        const val UNAVAILABLE = "Действие недоступно: статус заказа уже изменился"
    }
}
