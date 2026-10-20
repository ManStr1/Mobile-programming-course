package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ShoppingCart")
data class ShoppingCart(
    @PrimaryKey(autoGenerate = true) val cartId: Long = 0,
    val userId: Long?, // null = guest
    val createdAt: Long = System.currentTimeMillis()
)
