package avito.testtask.bookly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import avito.testtask.bookly.ui.screens.AuthScreen
import avito.testtask.bookly.ui.screens.BooksScreen
import avito.testtask.bookly.ui.screens.ProfileScreen
import avito.testtask.bookly.ui.screens.ReadingScreeen
import avito.testtask.bookly.ui.screens.UploadBookScreen
import avito.testtask.bookly.ui.theme.BooklyTheme
import avito.testtask.bookly.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BooklyTheme {
                BookReaderApp()
            }
        }
    }
}

@Composable
fun BookReaderApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is avito.testtask.domain.models.AuthState.Authenticated -> {
                if (navController.currentDestination?.route != "main") {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
            is avito.testtask.domain.models.AuthState.Unauthenticated -> {
                navController.navigate("auth") {
                    popUpTo("main") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(navController)
        }
        composable("main") {
            MainScreen(navController)
        }
        composable("reader/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            ReadingScreeen(navController, bookId)
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    var currentScreen by remember { mutableStateOf(Screen.Books) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = {
                            currentScreen = screen
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                Screen.Books -> BooksScreen(navController)
                Screen.Upload -> UploadBookScreen(navController)
                Screen.Profile -> ProfileScreen(navController)
            }
        }
    }
}

enum class Screen(
    val title: String,
    val icon: ImageVector
) {
    Books("Мои книги", Icons.Default.Book),
    Upload("Загрузка", Icons.Default.Download),
    Profile("Профиль", Icons.Default.Person)
}

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            BooklyTheme {
//                BookReaderApp()
//            }
//        }
//    }
//}
//
//@Composable
//fun BookReaderApp() {
//    val navController = rememberNavController()
//    val authViewModel: AuthViewModel = koinViewModel()
//    val authState by authViewModel.authState.collectAsState()
//
//    LaunchedEffect(Unit) {
//        if (authState is avito.testtask.domain.models.AuthState.Authenticated) {
//            navController.navigate("main") {
//                popUpTo("auth") { inclusive = true }
//            }
//        }
//    }
//
//    LaunchedEffect(authState) {
//        when (authState) {
//            is avito.testtask.domain.models.AuthState.Authenticated -> {
//                if (navController.currentDestination?.route != "main") {
//                    navController.navigate("main") {
//                        popUpTo("auth") { inclusive = true }
//                    }
//                }
//            }
//            is avito.testtask.domain.models.AuthState.Unauthenticated -> {
//                navController.navigate("auth") {
//                    popUpTo("main") { inclusive = true }
//                }
//            }
//            else -> {}
//        }
//    }
//
//    NavHost(
//        navController = navController,
//        startDestination = "auth"
//    ) {
//        composable("auth") {
//            AuthScreen(navController)
//        }
//        composable("main") {
//            MainScreen(navController)
//        }
//        composable("reader/{bookId}") { backStackEntry ->
//            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
//            ReadingScreeen(navController, bookId)
//        }
//    }
//}
//
//@Composable
//fun MainScreen(navController: NavHostController) {
//    val items = listOf(
//        Screen.Books,
//        Screen.Upload,
//        Screen.Profile
//    )
//
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = navBackStackEntry?.destination
//
//    val currentScreen = items.find { it.route == currentDestination?.route } ?: Screen.Books
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar {
//                items.forEach { screen ->
//                    NavigationBarItem(
//                        icon = { Icon(screen.icon, contentDescription = screen.title) },
//                        label = { Text(screen.title) },
//                        selected = currentScreen == screen,
//                        onClick = {
//                            if (currentScreen != screen) {
//                                navController.navigate(screen.route) {
//                                    popUpTo(navController.graph.findStartDestination().id) {
//                                        saveState = true
//                                    }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    ) { padding ->
//        NavHost(
//            navController = navController,
//            startDestination = Screen.Books.route,
//            modifier = Modifier.padding(padding)
//        ) {
//            composable(Screen.Books.route) {
//                BooksScreen(navController)
//            }
//            composable(Screen.Upload.route) {
//                UploadBookScreen(navController)
//            }
//            composable(Screen.Profile.route) {
//                ProfileScreen(navController)
//            }
//        }
//    }
//}
//
//sealed class Screen(
//    val route: String,
//    val title: String,
//    val icon: ImageVector
//) {
//    object Books : Screen("books", "Мои книги", Icons.Default.Book)
//    object Upload : Screen("upload", "Загрузка", Icons.Default.Download)
//    object Profile : Screen("profile", "Профиль", Icons.Default.Person)
//}