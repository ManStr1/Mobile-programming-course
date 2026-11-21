package com.example.mynotes.ui.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.mynotes.data.dao.CartItemWithInfo
import com.example.mynotes.data.entity.UserAddress
import com.example.mynotes.ui.common.EmptyState
import com.example.mynotes.ui.common.priceStr
import com.example.mynotes.viewmodel.AddressViewModel
import com.example.mynotes.viewmodel.CartViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartVm: CartViewModel,
    addressVm: AddressViewModel,
    isAuthed: Boolean,
    onAddAddress: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    onQtyChange: (cartItemId: Long, newQty: Int) -> Unit
) {
    val items by cartVm.items.collectAsStateWithLifecycle()
    val total by cartVm.total.collectAsStateWithLifecycle()
    val addresses by addressVm.addresses.collectAsStateWithLifecycle()

    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selected = addresses.firstOrNull { it.addressId == selectedId } ?: addresses.firstOrNull()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Корзина") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Итого: ${priceStr(total)}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (isAuthed) {
                    Button(
                        onClick = {
                            val address = selected ?: return@Button
                            cartVm.checkout(address) { orderId, error ->
                                if (orderId != null) {
                                    onOrderPlaced(orderId)
                                } else {
                                    scope.launch { snackbar.showSnackbar(error ?: "Не удалось оформить заказ") }
                                }
                            }
                        },
                        enabled = items.isNotEmpty() && selected != null,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Оформить заказ") }
                } else {
                    Text("Войдите в профиле, чтобы оформить заказ", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    ) { pad ->
        if (items.isEmpty()) {
            EmptyState("Корзина пуста. Добавьте товары на главной.", modifier = Modifier.padding(pad))
        } else {
            LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAuthed) {
                    item(key = "delivery") {
                        DeliverySection(
                            addresses = addresses,
                            selectedId = selected?.addressId,
                            onSelect = { selectedId = it },
                            onAddAddress = onAddAddress
                        )
                    }
                }
                items(items, key = { it.cartItemId }) { row ->
                    CartRow(row, onQtyChange)
                }
            }
        }
    }
}

@Composable
private fun DeliverySection(
    addresses: List<UserAddress>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddAddress: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Адрес доставки", style = MaterialTheme.typography.titleMedium)
            if (addresses.isEmpty()) {
                Text("У вас пока нет адресов", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onAddAddress) { Text("Добавить адрес") }
            } else {
                addresses.forEach { a ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(a.addressId) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = a.addressId == selectedId, onClick = { onSelect(a.addressId) })
                        Text(
                            "${a.street}, ${a.city}, ${a.state}, ${a.zipCode}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(
    row: CartItemWithInfo,
    onQtyChange: (Long, Int) -> Unit
) {
    val ctx = LocalContext.current

    ElevatedCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(row.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = row.name,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_delete)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(row.name, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                Text("Цена: ${priceStr(row.price)}", style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onQtyChange(row.cartItemId, -1) },
                    modifier = Modifier.size(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("−", fontSize = 18.sp, textAlign = TextAlign.Center)
                }

                Text(
                    text = "${row.quantity}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = { onQtyChange(row.cartItemId, +1) },
                    modifier = Modifier.size(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
