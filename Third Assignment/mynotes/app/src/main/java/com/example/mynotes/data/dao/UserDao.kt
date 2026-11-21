package com.example.mynotes.data.dao
import androidx.room.*
import com.example.mynotes.data.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM Users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM Users WHERE userId = :id LIMIT 1")
    suspend fun findById(id: Long): User?
}
