package com.example.mynotes.data.dao
import androidx.room.*
import com.example.mynotes.data.entity.CartItem
import com.example.mynotes.data.entity.ShoppingCart
import kotlinx.coroutines.flow.Flow

data class CartItemWithInfo(
    val cartItemId: Long,
    val productId: Long,
    val name: String,
    val price: Long,
    val imageUrl: String,
    val quantity: Int
)

@Dao
interface CartDao {
    @Insert
    suspend fun createCart(cart: ShoppingCart): Long

    @Query("SELECT * FROM ShoppingCart ORDER BY cartId LIMIT 1")
    suspend fun getAnyCart(): ShoppingCart?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CartItem): Long

    @Query("SELECT * FROM CartItems WHERE cartItemId = :id LIMIT 1")
    suspend fun findItemById(id: Long): CartItem?

    @Update
    suspend fun updateItem(item: CartItem)

    @Delete
    suspend fun deleteItem(item: CartItem)

    @Query("DELETE FROM CartItems WHERE cartId = :cartId")
    suspend fun clearCart(cartId: Long)

    @Query("""
      SELECT ci.cartItemId, ci.productId, p.name, p.price, p.imageUrl, ci.quantity
      FROM CartItems ci 
      JOIN Products p ON p.productId = ci.productId
      WHERE ci.cartId = :cartId
      ORDER BY ci.cartItemId DESC
    """)
    fun observeCartItems(cartId: Long): Flow<List<CartItemWithInfo>>

    @Query("SELECT * FROM CartItems WHERE cartId = :cartId AND productId = :productId LIMIT 1")
    suspend fun findItem(cartId: Long, productId: Long): CartItem?
}
