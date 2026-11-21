package com.example.mynotes.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.AppDatabase
import com.example.mynotes.data.entity.Product
import com.example.mynotes.data.repo.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ProductViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProductRepository(AppDatabase.get(app).productDao())

    private val _query = MutableStateFlow("")
    private val _categoryId = MutableStateFlow<Long?>(null)

    val query: StateFlow<String> = _query.asStateFlow()
    val selectedCategory: StateFlow<Long?> = _categoryId.asStateFlow()

    val categories = repo.categories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> =
        combine(_query, _categoryId) { q, c -> q to c }
            .flatMapLatest { (q, c) -> repo.products(q, c) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(id: Long?) { _categoryId.value = id }

    fun observeProduct(id: Long): Flow<Product?> = repo.observe(id)
}
