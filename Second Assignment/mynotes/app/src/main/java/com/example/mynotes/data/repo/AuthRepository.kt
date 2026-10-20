package com.example.mynotes.data.repo
import com.example.mynotes.data.dao.UserDao
import com.example.mynotes.data.entity.User
import java.security.MessageDigest

class AuthRepository(private val userDao: UserDao) {
    suspend fun register(username: String, email: String, password: String): Long {
        val hash = sha256(password)
        return userDao.insert(User(username = username, email = email, passwordHash = hash))
    }

    suspend fun login(username: String, password: String): User? {
        val u = userDao.findByUsername(username) ?: return null
        return if (u.passwordHash == sha256(password)) u else null
    }

    private fun sha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(s.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
