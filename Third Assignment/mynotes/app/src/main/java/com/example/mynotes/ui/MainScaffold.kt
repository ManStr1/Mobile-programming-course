package com.example.mynotes.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mynotes.navigation.Routes
import com.example.mynotes.ui.address.AddressFormScreen
import com.example.mynotes.ui.address.AddressesScreen
import com.example.mynotes.ui.cart.CartScreen
import com.example.mynotes.ui.home.HomeScreen
import com.example.mynotes.ui.orders.OrderDetailScreen
import com.example.mynotes.ui.orders.OrdersScreen
import com.example.mynotes.ui.product.ProductScreen
import com.example.mynotes.ui.profile.ProfileScreen
import com.example.mynotes.viewmodel.AddressViewModel
import com.example.mynotes.viewmodel.AuthViewModel
import com.example.mynotes.viewmodel.CartViewModel
import com.example.mynotes.viewmodel.OrderViewModel
import com.example.mynotes.viewmodel.ProductViewModel

@Composable
fun MainScaffold(
    authVm: AuthViewModel,
    cartVm: CartViewModel,
    prodVm: ProductViewModel,
    addressVm: AddressViewModel,
    orderVm: OrderViewModel
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
                val cartIds by cartVm.productIds.collectAsStateWithLifecycle()
                HomeScreen(
                    prodVm = prodVm,
                    inCartIds = cartIds,
                    onOpenProduct = { id -> nav.navigate(Routes.productWithId(id)) },
                    onAddToCart = { id -> cartVm.add(id) }
                )
            }
            composable(Routes.Cart) {
                val user by authVm.userId.collectAsStateWithLifecycle()
                CartScreen(
                    cartVm = cartVm,
                    addressVm = addressVm,
                    isAuthed = user != null,
                    onAddAddress = { nav.navigate(Routes.addressForm(null)) },
                    onOrderPlaced = { id -> nav.navigate(Routes.orderDetail(id)) },
                    onQtyChange = { cartItemId, delta -> cartVm.changeQty(cartItemId, delta) }
                )
            }
            composable(Routes.Profile) {
                ProfileScreen(
                    authVm = authVm,
                    onOpenAddresses = { nav.navigate(Routes.Addresses) },
                    onOpenOrders = { nav.navigate(Routes.Orders) }
                )
            }
            composable(Routes.Addresses) {
                val user by authVm.userId.collectAsStateWithLifecycle()
                AddressesScreen(
                    addressVm = addressVm,
                    isAuthed = user != null,
                    onAdd = { nav.navigate(Routes.addressForm(null)) },
                    onEdit = { id -> nav.navigate(Routes.addressForm(id)) },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                Routes.AddressFormRoute,
                arguments = listOf(navArgument(Routes.AddressArg) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { back ->
                val raw = back.arguments?.getLong(Routes.AddressArg) ?: -1L
                AddressFormScreen(
                    addressVm = addressVm,
                    addressId = raw.takeIf { it > 0 },
                    onDone = { nav.popBackStack() },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.Orders) {
                val user by authVm.userId.collectAsStateWithLifecycle()
                OrdersScreen(
                    orderVm = orderVm,
                    isAuthed = user != null,
                    onOpen = { id -> nav.navigate(Routes.orderDetail(id)) },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                Routes.OrderDetailRoute,
                arguments = listOf(navArgument(Routes.OrderArg) { type = NavType.LongType })
            ) { back ->
                val id = back.arguments?.getLong(Routes.OrderArg) ?: -1L
                OrderDetailScreen(
                    orderVm = orderVm,
                    orderId = id,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.ProductRoute) { back ->
                val id = back.arguments?.getString(Routes.ProductArg)?.toLongOrNull()
                val cartIds by cartVm.productIds.collectAsStateWithLifecycle()
                ProductScreen(
                    productId = id,
                    prodVm = prodVm,
                    inCart = id != null && id in cartIds,
                    onAddToCart = { id2 -> cartVm.add(id2) }
                )
            }
        }
    }
}
