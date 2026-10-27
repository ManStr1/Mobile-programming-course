package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Products")
data class Product(
    @PrimaryKey(autoGenerate = true) val productId: Long = 0,
    val name: String,
    val description: String,
    val price: Long,
    val imageUrl: String,
    val category: Long
)
