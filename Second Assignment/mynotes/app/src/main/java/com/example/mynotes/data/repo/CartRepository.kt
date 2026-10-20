package com.example.mynotes.data.repo
import com.example.mynotes.data.dao.CartDao
import com.example.mynotes.data.dao.CartItemWithInfo
import com.example.mynotes.data.entity.CartItem
import com.example.mynotes.data.entity.ShoppingCart
import kotlinx.coroutines.flow.Flow

class CartRepository(private val dao: CartDao) {
    suspend fun ensureCart(userId: Long?): Long {
        val existing = dao.getAnyCart()
        return if (existing == null) dao.createCart(ShoppingCart(userId = userId)) else existing.cartId
    }

    fun items(cartId: Long): Flow<List<CartItemWithInfo>> = dao.observeCartItems(cartId)

    suspend fun add(cartId: Long, productId: Long) {
        val found = dao.findItem(cartId, productId)
        if (found == null) dao.insertItem(CartItem(cartId = cartId, productId = productId, quantity = 1))
        else dao.updateItem(found.copy(quantity = found.quantity + 1))
    }

    suspend fun changeQty(cartItemId: Long, delta: Int) {
        val item = dao.findItemById(cartItemId) ?: return
        val newQty = item.quantity + delta
        when {
            newQty <= 0 -> dao.deleteItem(item)
            newQty != item.quantity -> dao.updateItem(item.copy(quantity = newQty))
        }
    }

    suspend fun clear(cartId: Long) = dao.clearCart(cartId)
}
