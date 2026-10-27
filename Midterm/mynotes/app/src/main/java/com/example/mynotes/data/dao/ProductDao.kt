package com.example.mynotes.data.dao
import androidx.room.*
import com.example.mynotes.data.entity.Category
import com.example.mynotes.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM Products ORDER BY productId DESC")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM Products WHERE name LIKE '%' || :q || '%' ORDER BY productId DESC")
    fun observeByQuery(q: String): Flow<List<Product>>

    @Query("SELECT * FROM Products WHERE (:categoryId IS NULL) OR (category = :categoryId) ORDER BY productId DESC")
    fun observeByCategory(categoryId: Long?): Flow<List<Product>>

    @Query("""
        SELECT * FROM Products 
        WHERE name LIKE '%' || :q || '%' 
        AND ((:categoryId IS NULL) OR (category = :categoryId))
        ORDER BY productId DESC
    """)
    fun observeByQueryAndCategory(q: String, categoryId: Long?): Flow<List<Product>>

    @Query("SELECT * FROM Products WHERE productId = :id LIMIT 1")
    suspend fun getById(id: Long): Product?

    // categories
    @Query("SELECT * FROM Categories ORDER BY categoryName ASC")
    fun observeCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(list: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(list: List<Category>)

    @Query("SELECT COUNT(*) FROM Products")
    suspend fun countProducts(): Int
}
