package com.example.mynotes.data.dao
import androidx.room.*
import com.example.mynotes.data.OrderStatus
import com.example.mynotes.data.entity.Order
import com.example.mynotes.data.entity.OrderItem
import com.example.mynotes.data.entity.Payment
import kotlinx.coroutines.flow.Flow

data class OrderItemInfo(
    val orderItemId: Long,
    val productId: Long,
    val name: String,
    val quantity: Int,
    val price: Long
)

data class CartLine(val productId: Long, val quantity: Int, val price: Long)

@Dao
abstract class OrderDao {

    @Query("SELECT * FROM Orders WHERE userId = :userId ORDER BY orderId DESC")
    abstract fun observeByUser(userId: Long): Flow<List<Order>>

    @Query("SELECT * FROM Orders WHERE orderId = :orderId LIMIT 1")
    abstract fun observeById(orderId: Long): Flow<Order?>

    @Query("""
        SELECT oi.orderItemId, oi.productId, p.name, oi.quantity, oi.price
        FROM OrderItems oi
        JOIN Products p ON p.productId = oi.productId
        WHERE oi.orderId = :orderId
        ORDER BY oi.orderItemId
    """)
    abstract fun observeItems(orderId: Long): Flow<List<OrderItemInfo>>

    @Query("SELECT * FROM Payments WHERE orderId = :orderId ORDER BY paymentDate DESC")
    abstract fun observePayments(orderId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM Orders WHERE orderId = :orderId LIMIT 1")
    abstract suspend fun getById(orderId: Long): Order?

    @Query("""
        SELECT ci.productId, ci.quantity, p.price
        FROM CartItems ci
        JOIN Products p ON p.productId = ci.productId
        WHERE ci.cartId = :cartId
    """)
    abstract suspend fun cartLines(cartId: Long): List<CartLine>

    @Query("DELETE FROM CartItems WHERE cartId = :cartId")
    abstract suspend fun clearCart(cartId: Long)

    @Insert
    abstract suspend fun insertOrder(order: Order): Long

    @Insert
    abstract suspend fun insertOrderItems(items: List<OrderItem>)

    @Insert
    abstract suspend fun insertPayment(payment: Payment): Long


    @Query("UPDATE Orders SET status = :to WHERE orderId = :orderId AND status = :from")
    abstract suspend fun moveStatus(orderId: Long, from: String, to: String): Int


    @Transaction
    open suspend fun placeOrder(userId: Long, cartId: Long, addressText: String, now: Long): Long? {
        val lines = cartLines(cartId)
        if (lines.isEmpty()) return null
        val total = lines.sumOf { it.price * it.quantity }
        val orderId = insertOrder(
            Order(
                userId = userId,
                orderDate = now,
                totalAmount = total,
                status = OrderStatus.NEW.name,
                addressText = addressText
            )
        )
        insertOrderItems(lines.map {
            OrderItem(orderId = orderId, productId = it.productId, quantity = it.quantity, price = it.price)
        })
        clearCart(cartId)
        return orderId
    }


    @Transaction
    open suspend fun pay(orderId: Long, method: String, now: Long): Boolean {
        val order = getById(orderId) ?: return false
        if (moveStatus(orderId, OrderStatus.NEW.name, OrderStatus.PAID.name) == 0) return false
        insertPayment(Payment(orderId = orderId, amount = order.totalAmount, paymentDate = now, paymentMethod = method))
        return true
    }
}
