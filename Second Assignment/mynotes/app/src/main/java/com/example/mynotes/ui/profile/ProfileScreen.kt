package com.example.mynotes.ui.profile
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mynotes.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authVm: AuthViewModel,
    onLoggedIn: (Long) -> Unit
) {
    val userId by authVm.userId.collectAsState()

    // СТАРОЕ                                                                                                   asdasdasd
    Scaffold(topBar = { TopAppBar(title = { Text("Профиль") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (userId == null) {
                Text("Вы не авторизованы")
                var username by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                OutlinedTextField(username, { username = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(password, { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        authVm.login(username, password) { ok ->
                            if (ok) authVm.userId.value?.let(onLoggedIn)
                        }
                    }) { Text("Войти") }
                    OutlinedButton(onClick = {
                        authVm.register(username, email, password) { ok ->
                            if (ok) authVm.userId.value?.let(onLoggedIn)
                        }
                    }) { Text("Создать аккаунт") }
                }
            } else {
                Text("Вы вошли как #$userId")
                Button(onClick = { authVm.logout() }) { Text("Выйти") }
                // позже здесь информация о заказах/адресах
            }
        }
    }
}
