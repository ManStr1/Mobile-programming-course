package com.example.mynotes.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mynotes.data.OrderStatus
import com.example.mynotes.ui.common.ConfirmDialog
import com.example.mynotes.ui.common.EmptyState
import com.example.mynotes.ui.common.formatDate
import com.example.mynotes.ui.common.priceStr
import com.example.mynotes.viewmodel.OrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderVm: OrderViewModel,
    orderId: Long,
    onBack: () -> Unit
) {
    LaunchedEffect(orderId) { orderVm.selectOrder(orderId) }
    DisposableEffect(orderId) { onDispose { orderVm.selectOrder(null) } }

    val details by orderVm.details.collectAsStateWithLifecycle()
    var confirmCancel by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun notify(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказ #$orderId") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        val d = details
        if (d == null) {
            EmptyState("Заказ не найден", modifier = Modifier.padding(pad))
        } else {
            val status = OrderStatus.of(d.order.status)
            Column(
                Modifier
                    .padding(pad)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusTimeline(status)
                Text("Дата: ${formatDate(d.order.orderDate)}", style = MaterialTheme.typography.bodyMedium)
                Text("Доставка: ${d.order.addressText}", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(4.dp))
                Text("Товары", style = MaterialTheme.typography.titleMedium)
                d.items.forEach { item ->
                    Text(
                        "${item.name} × ${item.quantity} — ${priceStr(item.price * item.quantity)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text("Итого: ${priceStr(d.order.totalAmount)}", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(4.dp))
                Text("Оплаты", style = MaterialTheme.typography.titleMedium)
                if (d.payments.isEmpty()) {
                    Text("Оплаты пока нет", style = MaterialTheme.typography.bodyMedium)
                } else {
                    d.payments.forEach { p ->
                        Text(
                            "${p.paymentMethod}: ${priceStr(p.amount)}, ${formatDate(p.paymentDate)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                when (status) {
                    OrderStatus.NEW -> {
                        Button(
                            onClick = { orderVm.pay(d.order.orderId) { notify(it) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Оплатить") }
                        OutlinedButton(
                            onClick = { confirmCancel = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Отменить заказ") }
                        Text("Отменить можно только до оплаты.", style = MaterialTheme.typography.bodySmall)
                    }
                    OrderStatus.PAID -> Button(
                        onClick = { orderVm.ship(d.order.orderId) { notify(it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Отправить (имитация курьера)") }
                    OrderStatus.SHIPPED -> Button(
                        onClick = { orderVm.deliver(d.order.orderId) { notify(it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Подтвердить получение") }
                    OrderStatus.DELIVERED, OrderStatus.CANCELLED, null ->
                        Text("С заказом больше нет действий.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (confirmCancel) {
        ConfirmDialog(
            title = "Отменить заказ?",
            text = "Заказ будет отменён. Восстановить его нельзя.",
            confirmLabel = "Отменить заказ",
            onConfirm = {
                confirmCancel = false
                orderVm.cancel(orderId) { notify(it) }
            },
            onDismiss = { confirmCancel = false }
        )
    }
}

@Composable
private fun StatusTimeline(current: OrderStatus?) {
    if (current == OrderStatus.CANCELLED) {
        Text("Заказ отменён", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        return
    }
    val steps = listOf(OrderStatus.NEW, OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED)
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        steps.forEach { step ->
            val reached = current != null && steps.indexOf(step) <= steps.indexOf(current)
            Text(
                step.label,
                style = MaterialTheme.typography.bodySmall,
                color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
