package com.example.mynotes.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserAddresses")
data class UserAddress(
    @PrimaryKey(autoGenerate = true) val addressId: Long = 0,
    val userId: Long,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String
)
