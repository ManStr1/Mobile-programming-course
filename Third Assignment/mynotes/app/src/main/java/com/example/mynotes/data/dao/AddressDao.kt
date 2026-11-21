package com.example.mynotes.data.dao
import androidx.room.*
import com.example.mynotes.data.entity.UserAddress
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM UserAddresses WHERE userId = :userId ORDER BY addressId DESC")
    fun observeByUser(userId: Long): Flow<List<UserAddress>>

    @Query("SELECT * FROM UserAddresses WHERE addressId = :id LIMIT 1")
    suspend fun getById(id: Long): UserAddress?

    @Insert
    suspend fun insert(address: UserAddress): Long

    @Update
    suspend fun update(address: UserAddress)

    @Delete
    suspend fun delete(address: UserAddress)
}
