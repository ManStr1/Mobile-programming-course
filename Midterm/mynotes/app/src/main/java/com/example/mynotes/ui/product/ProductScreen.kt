package com.example.mynotes.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.mynotes.data.entity.Product
import com.example.mynotes.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productId: Long?,
    prodVm: ProductViewModel,
    inCart: Boolean,
    onAddToCart: (Long) -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productId) {
        if (productId != null) product = prodVm.get(productId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(product?.name ?: "Товар") }) },
        bottomBar = {
            if (product != null)
                Button(
                    onClick = { onAddToCart(product!!.productId) },
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) { Text(if(inCart) "В корзине" else "Добавить в корзину") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val ctx = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(product?.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product?.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_delete)
            )


            Text(product?.name ?: "", style = MaterialTheme.typography.headlineSmall)
            Text(product?.description ?: "", style = MaterialTheme.typography.bodyMedium)
            Text(product?.let { priceStr(it.price) } ?: "", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun priceStr(price: Long): String = "%,d ₸".format(price).replace(',', ' ')
