package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.entity.BookWithInfo
import com.example.myapplication.ui.viewmodel.BookViewModel
import com.example.myapplication.ui.components.SortFilterBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onAddBookClick: () -> Unit,
    onBookClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
) {
    val books by viewModel.allBooks.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val readingStatus by viewModel.filterReadingStatus.collectAsState()
    
    var showSortFilterSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<BookWithInfo?>(null) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Book List") },
                actions = {
                    IconButton(onClick = { showSortFilterSheet = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Sort & Filter")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBookClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val searchQuery by viewModel.searchQuery.collectAsState()
            
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSearch = { /* Do nothing, search is realtime */ },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search books...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                   if (searchQuery.isNotEmpty()) {
                       IconButton(onClick = { viewModel.setSearchQuery("") }) {
                           Icon(Icons.Default.Clear, contentDescription = "Clear")
                       }
                   }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}

            BookList(
                books = books,
                onBookClick = onBookClick,
                onBookLongClick = { book ->
                    bookToDelete = book
                    showDeleteDialog = true
                },
                modifier = Modifier.weight(1f) // Fill remaining space
            )
        }
        
        if (showSortFilterSheet) {
            SortFilterBottomSheet(
                onDismissRequest = { showSortFilterSheet = false },
                currentSortOrder = sortOrder,
                onSortOrderSelected = { viewModel.setSortOrder(it) },
                currentReadingStatus = readingStatus,
                onReadingStatusSelected = { viewModel.setFilterReadingStatus(it) },
                onClearFilters = { viewModel.clearFilters() }
            )
        }
        
        if (showDeleteDialog && bookToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Book") },
                text = { Text("Are you sure you want to delete \"${bookToDelete?.book?.title}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            bookToDelete?.let { viewModel.deleteBook(it.book.id) }
                            showDeleteDialog = false
                            bookToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BookList(
    books: List<BookWithInfo>,
    onBookClick: (Long) -> Unit,
    onBookLongClick: (BookWithInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(books) { bookWithInfo ->
            BookItem(
                bookWithInfo = bookWithInfo,
                onClick = { onBookClick(bookWithInfo.book.id) },
                onLongClick = { onBookLongClick(bookWithInfo) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    bookWithInfo: BookWithInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = bookWithInfo.book.title, style = MaterialTheme.typography.titleMedium)
            val authors = bookWithInfo.authors.joinToString(", ") { it.name }
            if (authors.isNotBlank()) {
                Text(text = authors, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Text(text = "Status: ${bookWithInfo.book.readingStatus}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
