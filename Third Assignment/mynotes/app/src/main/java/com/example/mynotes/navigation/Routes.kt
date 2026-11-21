package com.example.mynotes.navigation
object Routes {
    const val Home = "home"
    const val Cart = "cart"
    const val Profile = "profile"
    const val Addresses = "addresses"
    const val Orders = "orders"

    const val Product = "product"
    const val ProductArg = "productId"
    const val ProductRoute = "$Product?${ProductArg}={${ProductArg}}"
    fun productWithId(id: Long) = "$Product?$ProductArg=$id"

    const val AddressForm = "address_form"
    const val AddressArg = "addressId"
    const val AddressFormRoute = "$AddressForm?$AddressArg={$AddressArg}"
    fun addressForm(id: Long?) = "$AddressForm?$AddressArg=${id ?: -1L}"

    const val OrderDetail = "order"
    const val OrderArg = "orderId"
    const val OrderDetailRoute = "$OrderDetail/{$OrderArg}"
    fun orderDetail(id: Long) = "$OrderDetail/$id"
}

