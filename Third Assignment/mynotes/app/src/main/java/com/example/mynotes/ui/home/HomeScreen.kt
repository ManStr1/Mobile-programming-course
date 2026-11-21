package com.example.mynotes.ui.home
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.mynotes.data.entity.Category
import com.example.mynotes.data.entity.Product
import com.example.mynotes.ui.common.EmptyState
import com.example.mynotes.ui.common.priceStr
import com.example.mynotes.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    prodVm: ProductViewModel,
    inCartIds: Set<Long>,
    onOpenProduct: (Long) -> Unit,
    onAddToCart: (Long) -> Unit
) {
    val categories by prodVm.categories.collectAsStateWithLifecycle()
    val products by prodVm.products.collectAsStateWithLifecycle()
    val query by prodVm.query.collectAsStateWithLifecycle()
    val selectedCat by prodVm.selectedCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Магазин") }) }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { prodVm.setQuery(it) },
                placeholder = { Text("Поиск по товарам") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            CategoryChips(categories, selectedCat) { prodVm.setCategory(it) }

            if (products.isEmpty()) {
                EmptyState(
                    if (query.isBlank() && selectedCat == null) "Товаров пока нет"
                    else "По вашему запросу ничего не найдено"
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products, key = { it.productId }) { p ->
                        ProductRow(
                            p,
                            inCart = p.productId in inCartIds,
                            onOpen = { onOpenProduct(p.productId) },
                            onAdd = { onAddToCart(p.productId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<Category>,
    selected: Long?,
    onSelect: (Long?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == null, onClick = { onSelect(null) },
            label = { Text("Все") }
        )
        categories.forEach { c ->
            FilterChip(selected = selected == c.categoryId, onClick = { onSelect(c.categoryId) }, label = { Text(c.categoryName) })
        }
    }
}

@Composable
private fun ProductRow(p: Product, inCart: Boolean, onOpen: () -> Unit, onAdd: () -> Unit) {
    ElevatedCard(onClick = onOpen) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(p.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = p.name,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_delete)
            )

            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(priceStr(p.price), style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onAdd) { Text(if (inCart) "В корзине" else "В корзину") }
        }
    }
}
