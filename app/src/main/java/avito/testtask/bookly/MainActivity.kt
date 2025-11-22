import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BooklyAppTheme {
                BookReaderApp()
            }
        }
    }
}

@Composable
fun BookReaderApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BookReaderBottomNavigation(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Books.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Books.route) { BooksScreen(navController) }
            composable(Screen.Upload.route) { UploadScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController) }
            composable(
                route = Screen.ReadBook.route + "/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                ReadBookScreen(navController, bookId)
            }
        }
    }
}

@Composable
fun BookReaderBottomNavigation(navController: NavController) {
    val currentRoute = currentRoute(navController)

    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Book, contentDescription = "Книги") },
            label = { Text("Книги") },
            selected = currentRoute == Screen.Books.route,
            onClick = { navController.navigate(Screen.Books.route) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Upload, contentDescription = "Загрузка") },
            label = { Text("Загрузка") },
            selected = currentRoute == Screen.Upload.route,
            onClick = { navController.navigate(Screen.Upload.route) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label = { Text("Профиль") },
            selected = currentRoute == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) }
        )
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Books : Screen("books")
    object Upload : Screen("upload")
    object Profile : Screen("profile")
    object ReadBook : Screen("read_book")
}