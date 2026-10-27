package com.example.mynotes.data.repo
import com.example.mynotes.data.dao.ProductDao

class ProductRepository(private val dao: ProductDao) {
    fun categories() = dao.observeCategories()
    fun products(q: String, categoryId: Long?) = dao.observeByQueryAndCategory(q, categoryId)
    suspend fun get(id: Long) = dao.getById(id)
}
