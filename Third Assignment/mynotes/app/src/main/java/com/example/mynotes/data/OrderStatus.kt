package com.example.mynotes.data

enum class OrderStatus(val label: String) {
    NEW("Новый"),
    PAID("Оплачен"),
    SHIPPED("Отправлен"),
    DELIVERED("Получен"),
    CANCELLED("Отменён");

    companion object {
        fun of(name: String): OrderStatus? = values().firstOrNull { it.name == name }
    }
}