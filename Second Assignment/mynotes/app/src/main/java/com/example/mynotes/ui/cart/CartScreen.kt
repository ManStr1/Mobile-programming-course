package com.example.mynotes.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.mynotes.data.dao.CartItemWithInfo
import com.example.mynotes.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartVm: CartViewModel,
    isAuthed: Boolean,
    onCheckout: () -> Unit,
    onQtyChange: (cartItemId: Long, newQty: Int) -> Unit
) {
    val items by cartVm.items.collectAsState()
    val total by cartVm.total.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Корзина") }) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Итого: ${priceStr(total)}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (isAuthed) {
                    Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) { Text("Оформить") }
                } else {
                    Text("Авторизуйтесь, чтобы оформить заказ", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    ) { pad ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Корзина пуста")
            }
        } else {
            LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.cartItemId }) { row ->
                    CartRow(row, onQtyChange)
                }
            }
        }
    }
}

@Composable
private fun CartRow(
    it: CartItemWithInfo,
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
                    .data(it.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = it.name,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_delete)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(it.name, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                Text("Цена: ${priceStr(it.price)}", style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onQtyChange(it.cartItemId, -1) },
                    modifier = Modifier.size(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("−", fontSize = 18.sp, textAlign = TextAlign.Center)
                }

                Text(
                    text = "${it.quantity}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = { onQtyChange(it.cartItemId, +1) },
                    modifier = Modifier.size(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}



private fun priceStr(price: Long): String = "%,d ₸".format(price).replace(',', ' ')
