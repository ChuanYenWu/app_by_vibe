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
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.ui.screens.EntityListScreen
import com.example.myapplication.ui.viewmodel.BookViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.myapplication.util.ParsedBookInfo

sealed class Screen(val route: String, val title: String = "") {
    object BookList : Screen("bookList", "Books")
    object AddBook : Screen("addBook", "Add Book")
    object EditBook : Screen("editBook/{bookId}", "Edit Book") {
        fun createRoute(bookId: Long) = "editBook/$bookId"
    }
    object Authors : Screen("authors", "Authors")
    object Genres : Screen("genres", "Genres")
    object Tags : Screen("tags", "Tags")
    object Settings : Screen("settings", "Settings")
    object BookDetail : Screen("bookDetail/{bookId}", "Book Details") {
        fun createRoute(bookId: Long) = "bookDetail/$bookId"
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    initialBookInfo: ParsedBookInfo? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BookList.route,
        modifier = modifier
    ) {
        composable(Screen.BookList.route) {
            BookListScreen(
                onAddBookClick = { navController.navigate(Screen.AddBook.route) },
                onBookClick = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.AddBook.route) {
            AddEditBookScreen(
                initialBookInfo = initialBookInfo,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { bookId -> 
                    navController.popBackStack() // Remove add screen
                    navController.navigate(Screen.BookDetail.createRoute(bookId)) 
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Authors.route) {
            val viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
            val items by viewModel.allAuthors.collectAsState(initial = emptyList())
            EntityListScreen(
                title = "Authors",
                items = items,
                onEdit = { author, name -> viewModel.updateAuthor(author.copy(name = name)) },
                onDelete = { viewModel.deleteAuthor(it) },
                nameProvider = { it.name }
            )
        }
        composable(Screen.Genres.route) {
            val viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
            val items by viewModel.allGenres.collectAsState(initial = emptyList())
            EntityListScreen(
                title = "Genres",
                items = items,
                onEdit = { genre, name -> viewModel.updateGenre(genre.copy(name = name)) },
                onDelete = { viewModel.deleteGenre(it) },
                nameProvider = { it.name }
            )
        }
        composable(Screen.Tags.route) {
            val viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
            val items by viewModel.allTags.collectAsState(initial = emptyList())
            EntityListScreen(
                title = "Tags",
                items = items,
                onEdit = { tag, name -> viewModel.updateTag(tag.copy(name = name)) },
                onDelete = { viewModel.deleteTag(it) },
                nameProvider = { it.name }
            )
        }
        composable(
            route = Screen.EditBook.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            AddEditBookScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id ->
                    navController.popBackStack()
                    navController.navigate(Screen.BookDetail.createRoute(id))
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
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Screen.EditBook.createRoute(id)) }
            )
        }
    }
}
