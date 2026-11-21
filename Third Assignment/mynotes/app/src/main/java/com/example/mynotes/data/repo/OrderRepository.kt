package com.example.mynotes.data.repo
import com.example.mynotes.data.dao.OrderDao
import com.example.mynotes.data.dao.OrderItemInfo
import com.example.mynotes.data.entity.Order
import com.example.mynotes.data.entity.Payment
import com.example.mynotes.data.entity.UserAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class OrderDetails(
    val order: Order,
    val items: List<OrderItemInfo>,
    val payments: List<Payment>
)

class OrderRepository(private val dao: OrderDao) {

    fun orders(userId: Long): Flow<List<Order>> = dao.observeByUser(userId)

    fun details(orderId: Long): Flow<OrderDetails?> = combine(
        dao.observeById(orderId),
        dao.observeItems(orderId),
        dao.observePayments(orderId)
    ) { order, items, payments ->
        order?.let { OrderDetails(it, items, payments) }
    }

    suspend fun placeOrder(userId: Long, cartId: Long, address: UserAddress): Long? =
        dao.placeOrder(userId, cartId, formatAddress(address), System.currentTimeMillis())

    suspend fun pay(orderId: Long): Boolean =
        dao.pay(orderId, PAYMENT_METHOD, System.currentTimeMillis())

    suspend fun ship(orderId: Long): Boolean = dao.moveStatus(orderId, "PAID", "SHIPPED") > 0

    suspend fun deliver(orderId: Long): Boolean = dao.moveStatus(orderId, "SHIPPED", "DELIVERED") > 0

    suspend fun cancel(orderId: Long): Boolean = dao.moveStatus(orderId, "NEW", "CANCELLED") > 0

    private fun formatAddress(a: UserAddress) = "${a.street}, ${a.city}, ${a.state}, ${a.zipCode}"

    companion object {
        const val PAYMENT_METHOD = "Банковская карта (тест)"
    }
}
