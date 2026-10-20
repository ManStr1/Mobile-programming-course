package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val reviewId: Long = 0,
    val productId: Long,
    val userId: Long,
    val rating: Int,
    val comment: String
)
