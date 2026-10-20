# MyNotes — mini-market Jetpack Compose

An educational Android app: product catalog, search and filter by category, product card, shopping cart, and simple login. Data is stored locally in Room.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose (bottom navigation bar)
- ViewModel + StateFlow / Flow (`AndroidViewModel`, `stateIn`, `flatMapLatest`)
- Room (SQLite, `shop.db` file)
- Coil 3 for Images

## Structure

```
com.example.mynotes
├── MainActivity.kt         — creates ViewModel and launch MainScaffold
├── MynotesApp.kt           — Application, Coil launch
├── navigation/Routes.kt    — Routes: home, cart, profile, product?productId
├── ui/
│   ├── MainScaffold.kt     — Scaffold, bottom navigation, NavHost
│   ├── home/HomeScreen.kt  — search, filter-tags, items list
│   ├── product/ProductScreen.kt
│   ├── cart/CartScreen.kt  — list of cart items, total sum, buttons +/−
│   ├── profile/ProfileScreen.kt — login, registration, logout
│   └── theme/              — colors, theme
├── viewmodel/
│   ├── AuthViewModel.kt    — userId, login, register, logout
│   ├── CartViewModel.kt    — items, total, add, changeQty
│   └── ProductViewModel.kt — products, categories, setQuery, setCategory
└── data/
    ├── AppDatabase.kt      — Room, creates Database and inital data
    ├── entity/             — Product, Category, User, ShoppingCart, CartItem etc.
    ├── dao/                — ProductDao, CartDao, UserDao
    └── repo/               — ProductRepository, CartRepository, AuthRepository[README.en.md](README.en.md)
```

## Как работает приложение

Data flows along the chain **Room → DAO → Repository → ViewModel → Composable**. User actions flow in the opposite direction: **Composable → ViewModel → Repository → DAO**.

- **Search and filter.** Typing in the search field calls `ProductViewModel.setQuery()`, and selecting a category chip calls `setCategory()`. The ViewModel combines both values with `combine` and `flatMapLatest` and queries products from Room. The SQL query `observeByQueryAndCategory` filters by name (`LIKE`) and category. The list updates automatically.
- **Cart.** The "Add to cart" button calls `CartViewModel.add()`. Quantity is changed with `changeQty()`. The cart screen subscribes to a Room Flow, so it updates by itself.
- **Total.** The sum (`total`) is computed in `CartViewModel` with `map` over the cart items.
- **Authentication.** `AuthViewModel` keeps `userId` as a `StateFlow`. Passwords are hashed with SHA-256 (`AuthRepository`).

## Key Compose components by screen

| Screen | Components |
|---|---|
| Home | `LazyColumn` with `ElevatedCard` items, `OutlinedTextField` (search), `FilterChip` (categories), `AsyncImage`, `Text`, `Button` |
| Product | `Scaffold` with `TopAppBar` and a bottom button, `AsyncImage`, `Text` |
| Cart | `LazyColumn` with `ElevatedCard`, `OutlinedButton` (− / +), `Text` with total |
| Profile | `OutlinedTextField`, `Button`, `OutlinedButton` |

## State

- **ViewModel** holds data that must outlive the screen: `userId`, the cart, the product list, the current search query and category.
- **`remember`** holds local screen state: the loaded product on `ProductScreen`, form fields on `ProfileScreen`, the search field and selected category on `HomeScreen`.
- **`rememberSaveable`** is not used in the project.

## Running the app

1. On the repository page, click **Code → Download ZIP**.
2. Extract the archive.
3. In Android Studio, select **File → Open** and choose the extracted folder (the one containing `settings.gradle.kts`).
4. Wait for **Gradle Sync** to finish, select an emulator or device, and click **Run ▶** (`Shift + F10`).

On first launch, the `shop.db` database is created automatically and filled with four products and three categories.

### Test actions

- Search: type "Pixel" or "ThinkPad" in the search field on the home screen.
- Filter: select the "Ноутбуки" (Laptops) or "Наушники" (Headphones) category chip.
- Cart: add a product from the home screen or the product screen, then change its quantity on the "Cart" tab.
- Login: go to the "Profile" tab → "Create account". After that, checkout becomes available.

## Limitations

- Checkout is not implemented yet (placeholder button).
- Data is stored only on the device; there is no server.
