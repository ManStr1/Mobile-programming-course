package com.example.mynotes.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mynotes.data.OrderStatus
import com.example.mynotes.data.entity.Order
import com.example.mynotes.ui.common.EmptyState
import com.example.mynotes.ui.common.formatDate
import com.example.mynotes.ui.common.priceStr
import com.example.mynotes.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    orderVm: OrderViewModel,
    isAuthed: Boolean,
    onOpen: (Long) -> Unit,
    onBack: () -> Unit
) {
    val orders by orderVm.orders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заказы") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { pad ->
        when {
            !isAuthed -> EmptyState("Войдите в профиле, чтобы увидеть заказы", modifier = Modifier.padding(pad))
            orders.isEmpty() -> EmptyState(
                "Заказов пока нет. Оформите заказ из корзины.",
                modifier = Modifier.padding(pad)
            )
            else -> LazyColumn(
                Modifier.padding(pad).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders, key = { it.orderId }) { order ->
                    OrderCard(order, onClick = { onOpen(order.orderId) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Заказ #${order.orderId}", style = MaterialTheme.typography.titleMedium)
            Text(formatDate(order.orderDate), style = MaterialTheme.typography.bodySmall)
            Text("Статус: ${OrderStatus.of(order.status)?.label ?: order.status}", style = MaterialTheme.typography.bodyMedium)
            Text("Сумма: ${priceStr(order.totalAmount)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
