package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OrderItems")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val orderItemId: Long = 0,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: Long
)
