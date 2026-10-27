package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val paymentId: Long = 0,
    val orderId: Long,
    val amount: Long,
    val paymentDate: Long,
    val paymentMethod: String
)
