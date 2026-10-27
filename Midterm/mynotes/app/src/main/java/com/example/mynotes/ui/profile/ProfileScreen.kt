package com.example.mynotes.ui.profile
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mynotes.viewmodel.AuthViewModel

private enum class AuthMode {LOGIN, REGISTER}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authVm: AuthViewModel) {
    val user by authVm.user.collectAsState()

    var mode by rememberSaveable { mutableStateOf(AuthMode.LOGIN)}
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }


    Scaffold(topBar = { TopAppBar(title = { Text("Профиль") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val current = user
            if (current == null) {
                when (mode) {
                    AuthMode.LOGIN -> {
                        Text("Вы не авторизованы", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Логин") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Пароль") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        info?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                        Button(
                            onClick = {
                                info = null
                                authVm.login(username, password) { err ->
                                    error = err
                                    if (err == null) password = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Войти") }
                        OutlinedButton(
                            onClick = {
                                mode = AuthMode.REGISTER
                                error = null
                                info = null
                                password = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Создать аккаунт") }
                    }

                    AuthMode.REGISTER -> {
                        Text("Новый аккаунт", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Логин (уникальный)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Пароль") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passwordRepeat,
                            onValueChange = { passwordRepeat = it },
                            label = { Text("Повторите пароль") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                        Button(
                            onClick = {
                                authVm.register(username, email, password, passwordRepeat) { err ->
                                    error = err
                                    if (err == null) {
                                        mode = AuthMode.LOGIN
                                        info = "Вы успешно зарегистрированы. Войдите в аккаунт."
                                        username = ""
                                        password = ""
                                        passwordRepeat = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Зарегистрироваться") }
                        OutlinedButton(
                            onClick = {
                                mode = AuthMode.LOGIN
                                error = null
                                password = ""
                                passwordRepeat = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Назад ко входу") }
                    }
                }
            } else {
                Text("Логин: ${current.username}", style = MaterialTheme.typography.titleMedium)
                Text("Почта: ${current.email}", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { authVm.logout() }) { Text("Выйти") }
            }
        }
    }
}
