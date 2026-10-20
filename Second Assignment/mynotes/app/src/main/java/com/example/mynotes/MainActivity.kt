package com.example.mynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynotes.ui.MainScaffold
import com.example.mynotes.ui.theme.AppTheme
import com.example.mynotes.viewmodel.AuthViewModel
import com.example.mynotes.viewmodel.CartViewModel
import com.example.mynotes.viewmodel.ProductViewModel
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authVm: AuthViewModel = viewModel()
            val cartVm: CartViewModel = viewModel()
            val prodVm: ProductViewModel = viewModel()

            cartVm.ensureCart(authVm.userId.collectAsState().value)

            AppTheme {
                MainScaffold(authVm, cartVm, prodVm)
            }
        }
    }
}

