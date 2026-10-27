package com.example.mynotes.navigation
object Routes {
    const val Home = "home"
    const val Cart = "cart"
    const val Profile = "profile"

    const val Product = "product"
    const val ProductArg = "productId"
    const val ProductRoute = "$Product?${ProductArg}={${ProductArg}}"

    fun productWithId(id: Long) = "$Product?$ProductArg=$id"
}

