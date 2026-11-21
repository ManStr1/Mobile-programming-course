package com.example.mynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynotes.ui.MainScaffold
import com.example.mynotes.ui.theme.AppTheme
import com.example.mynotes.viewmodel.AuthViewModel
import com.example.mynotes.viewmodel.CartViewModel
import com.example.mynotes.viewmodel.ProductViewModel
import androidx.compose.runtime.getValue
import com.example.mynotes.viewmodel.AddressViewModel
import com.example.mynotes.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authVm: AuthViewModel = viewModel()
            val cartVm: CartViewModel = viewModel()
            val prodVm: ProductViewModel = viewModel()
            val addressVm: AddressViewModel = viewModel()
            val orderVm: OrderViewModel = viewModel()


            val userId by authVm.userId.collectAsStateWithLifecycle()
            LaunchedEffect(userId) {
                cartVm.ensureCart(userId)
                addressVm.setUser(userId)
                orderVm.setUser(userId)
            }

            AppTheme {
                MainScaffold(authVm, cartVm, prodVm, addressVm, orderVm)
            }
        }
    }
}

