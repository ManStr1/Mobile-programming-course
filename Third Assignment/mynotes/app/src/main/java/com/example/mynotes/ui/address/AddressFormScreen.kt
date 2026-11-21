package com.example.mynotes.ui.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mynotes.viewmodel.AddressViewModel
import com.example.mynotes.viewmodel.SaveResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormScreen(
    addressVm: AddressViewModel,
    addressId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var street by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var loaded by rememberSaveable(addressId) { mutableStateOf(addressId == null) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(addressId) {
        if (addressId != null && !loaded) {
            addressVm.get(addressId)?.let { a ->
                street = a.street
                city = a.city
                region = a.state
                zip = a.zipCode
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addressId == null) "Новый адрес" else "Редактирование адреса") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddressField("Улица и дом", street, errors["street"]) { street = it }
            AddressField("Город", city, errors["city"]) { city = it }
            AddressField("Область", region, errors["region"]) { region = it }
            AddressField("Индекс (6 цифр)", zip, errors["zip"], KeyboardType.Number) { zip = it }

            Button(
                onClick = {
                    addressVm.save(addressId, street, city, region, zip) { result ->
                        when (result) {
                            is SaveResult.Saved -> onDone()
                            is SaveResult.Invalid -> { errors = result.errors }
                            is SaveResult.Failed -> { scope.launch { snackbar.showSnackbar(result.message) } }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сохранить") }
        }
    }
}

@Composable
private fun AddressField(
    label: String,
    value: String,
    error: String?,
    keyboard: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth()
    )
}
