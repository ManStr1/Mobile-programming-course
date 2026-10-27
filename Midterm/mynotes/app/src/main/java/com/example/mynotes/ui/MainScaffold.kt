package com.example.mynotes.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.mynotes.navigation.Routes
import com.example.mynotes.ui.cart.CartScreen
import com.example.mynotes.ui.home.HomeScreen
import com.example.mynotes.ui.product.ProductScreen
import com.example.mynotes.ui.profile.ProfileScreen
import com.example.mynotes.viewmodel.AuthViewModel
import com.example.mynotes.viewmodel.CartViewModel
import com.example.mynotes.viewmodel.ProductViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items



@Composable
fun MainScaffold(
    authVm: AuthViewModel,
    cartVm: CartViewModel,
    prodVm: ProductViewModel
) {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = {
            val entry by nav.currentBackStackEntryAsState()
            val current = entry?.destination?.route
            NavigationBar {
                NavigationBarItem(
                    selected = current == Routes.Home,
                    onClick = { nav.navigate(Routes.Home) { launchSingleTop = true; popUpTo(Routes.Home) } },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = current == Routes.Cart,
                    onClick = { nav.navigate(Routes.Cart) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    label = { Text("Корзина") }
                )
                NavigationBarItem(
                    selected = current == Routes.Profile,
                    onClick = { nav.navigate(Routes.Profile) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Профиль") }
                )
            }
        }
    ) { inner ->
        NavHost(navController = nav, startDestination = Routes.Home, modifier = Modifier.padding(inner)) {
            composable(Routes.Home) {
                val cartIds by cartVm.productIds.collectAsState()
                HomeScreen(
                    prodVm = prodVm,
                    inCartIds = cartIds,
                    onOpenProduct = { id -> nav.navigate(Routes.productWithId(id)) },
                    onAddToCart = { id -> cartVm.add(id) }
                )
            }
            composable(Routes.Cart) {
                val isAuthed = authVm.userId.collectAsState().value != null
                CartScreen(
                    cartVm = cartVm,
                    isAuthed = isAuthed,
                    onCheckout = { /* TODO: оформить заказ позже */ },
                    onQtyChange = { cartItemId, delta -> cartVm.changeQty(cartItemId, delta) }
                )
            }
            composable(Routes.Profile) {
                ProfileScreen(authVm = authVm)
            }
            composable(Routes.ProductRoute) { back ->
                val id = back.arguments?.getString(Routes.ProductArg)?.toLongOrNull()
                val cartIds by cartVm.productIds.collectAsState()
                ProductScreen(
                    productId = id,
                    prodVm = prodVm,
                    inCart = id != null && id in cartIds,
                    onAddToCart = { id2 -> cartVm.add(id2) })
            }
        }
    }
}

