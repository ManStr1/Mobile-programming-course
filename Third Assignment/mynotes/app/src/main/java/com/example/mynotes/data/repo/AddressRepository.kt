package com.example.mynotes.data.repo
import com.example.mynotes.data.dao.AddressDao
import com.example.mynotes.data.entity.UserAddress

class AddressRepository(private val dao: AddressDao) {
    fun observe(userId: Long) = dao.observeByUser(userId)

    suspend fun get(id: Long): UserAddress? = dao.getById(id)

    suspend fun save(
        existing: UserAddress?,
        userId: Long,
        street: String,
        city: String,
        region: String,
        zip: String
    ): Long {
        if (existing == null) {
            return dao.insert(UserAddress(userId = userId, street = street, city = city, state = region, zipCode = zip))
        }
        dao.update(existing.copy(street = street, city = city, state = region, zipCode = zip))
        return existing.addressId
    }

    suspend fun delete(address: UserAddress) = dao.delete(address)
}
