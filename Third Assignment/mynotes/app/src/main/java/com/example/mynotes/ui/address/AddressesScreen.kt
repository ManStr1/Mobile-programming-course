package com.example.mynotes.ui.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mynotes.ui.common.ConfirmDialog
import com.example.mynotes.ui.common.EmptyState
import com.example.mynotes.viewmodel.AddressViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    addressVm: AddressViewModel,
    isAuthed: Boolean,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit
) {
    val addresses by addressVm.addresses.collectAsStateWithLifecycle()
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои адреса") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (isAuthed) {
                FloatingActionButton(onClick = onAdd) { Text("+", style = MaterialTheme.typography.titleLarge) }
            }
        }
    ) { pad ->
        when {
            !isAuthed -> EmptyState("Войдите в профиле, чтобы управлять адресами", modifier = Modifier.padding(pad))
            addresses.isEmpty() -> EmptyState(
                "Адресов пока нет. Добавьте адрес доставки.",
                actionLabel = "Добавить адрес",
                onAction = onAdd,
                modifier = Modifier.padding(pad)
            )
            else -> LazyColumn(
                Modifier.padding(pad).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addresses, key = { it.addressId }) { a ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(a.street, style = MaterialTheme.typography.titleMedium)
                            Text("${a.city}, ${a.state}, ${a.zipCode}", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { onEdit(a.addressId) }) { Text("Изменить") }
                                TextButton(onClick = { deleteId = a.addressId }) { Text("Удалить") }
                            }
                        }
                    }
                }
            }
        }
    }

    addresses.firstOrNull { it.addressId == deleteId }?.let { address ->
        ConfirmDialog(
            title = "Удалить адрес?",
            text = "Адрес «${address.street}» будет удалён без возможности восстановления.",
            confirmLabel = "Удалить",
            onConfirm = {
                deleteId = null
                addressVm.delete(address) { msg -> scope.launch { snackbar.showSnackbar(msg) } }
            },
            onDismiss = { deleteId = null }
        )
    }
}
