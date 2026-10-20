package com.example.mynotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mynotes.data.dao.CartDao
import com.example.mynotes.data.dao.ProductDao
import com.example.mynotes.data.dao.UserDao
import com.example.mynotes.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class, Product::class, Category::class,
        ShoppingCart::class, CartItem::class,
        Order::class, OrderItem::class, Review::class, UserAddress::class, Payment::class
    ],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "shop.db"
                )
                    .build()
                    .also { db ->
                        INSTANCE = db

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val pDao = db.productDao()
                                if (pDao.countProducts() == 0) {
                                    val cats = listOf(
                                        Category(1, "Смартфоны"),
                                        Category(2, "Ноутбуки"),
                                        Category(3, "Наушники")
                                    )
                                    pDao.insertCategories(cats)

                                    val products = listOf(
                                        Product(name = "Pixel 8", description = "Флагман", price = 299900, imageUrl = "https://picsum.photos/seed/pixel8/600/400", category = 1),
                                        Product(name = "Galaxy S23", description = "Топовый Android", price = 399900, imageUrl = "https://picsum.photos/seed/galaxys23/600/400", category = 1),
                                        Product(name = "ThinkPad X1", description = "Бизнес-ноутбук", price = 899900, imageUrl = "https://picsum.photos/seed/x1/600/400", category = 2),
                                        Product(name = "AirPods Pro", description = "ANC", price = 149900, imageUrl = "https://picsum.photos/seed/airpods/600/400", category = 3),
                                    )
                                    pDao.insertProducts(products)
                                }
                            } catch (_: Throwable) { }
                        }
                    }
            }
    }
}