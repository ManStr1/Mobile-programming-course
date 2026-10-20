package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartItems")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val cartItemId: Long = 0,
    val cartId: Long,
    val productId: Long,
    val quantity: Int
)
