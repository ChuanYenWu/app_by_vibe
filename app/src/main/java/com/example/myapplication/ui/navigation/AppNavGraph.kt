package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.AddEditBookScreen
import com.example.myapplication.ui.screens.BookDetailScreen
import com.example.myapplication.ui.screens.BookListScreen

sealed class Screen(val route: String) {
    object BookList : Screen("bookList")
    object AddBook : Screen("addBook")
    object BookDetail : Screen("bookDetail/{bookId}") {
        fun createRoute(bookId: Long) = "bookDetail/$bookId"
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BookList.route,
        modifier = modifier
    ) {
        composable(Screen.BookList.route) {
            BookListScreen(
                onAddBookClick = { navController.navigate(Screen.AddBook.route) },
                onBookClick = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) }
            )
        }
        composable(Screen.AddBook.route) {
            AddEditBookScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { bookId -> 
                    navController.popBackStack() // Remove add screen
                    navController.navigate(Screen.BookDetail.createRoute(bookId)) 
                }
            )
        }
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            BookDetailScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
