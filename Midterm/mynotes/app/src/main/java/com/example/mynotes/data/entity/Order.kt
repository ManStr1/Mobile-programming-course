package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    val userId: Long,
    val orderDate: Long,
    val totalAmount: Long,
    val status: String
)
