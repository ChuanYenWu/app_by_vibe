package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.entity.Author
import com.example.myapplication.data.local.entity.Genre
import com.example.myapplication.data.local.entity.Tag
import com.example.myapplication.data.local.entity.Book
import com.example.myapplication.data.local.entity.BookWithInfo
import com.example.myapplication.data.local.entity.BookLink
import com.example.myapplication.ui.components.AutocompleteTagInput
import com.example.myapplication.ui.components.DynamicLinkList
import com.example.myapplication.ui.viewmodel.BookViewModel
import com.example.myapplication.util.ParsedBookInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    bookId: Long? = null,
    initialBookInfo: ParsedBookInfo? = null,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Existing Book Data (if editing)
    val existingBookWithInfo by if (bookId != null) {
        viewModel.getBookById(bookId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }
    
    // Data from ViewModel
    val allAuthors by viewModel.allAuthors.collectAsState()
    val allGenres by viewModel.allGenres.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    // Form State
    var title by remember { mutableStateOf("") }
    var readingStatus by remember { mutableStateOf("想讀") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) }

    // Selection State
    var selectedAuthors: List<Author> by remember { mutableStateOf(emptyList()) }
    var selectedGenres: List<Genre> by remember { mutableStateOf(emptyList()) }
    var selectedTags: List<Tag> by remember { mutableStateOf(emptyList()) }
    var links: List<BookLink> by remember { mutableStateOf(emptyList()) }

    // Load existing data logic
    LaunchedEffect(existingBookWithInfo) {
        existingBookWithInfo?.let { info ->
            if (title.isEmpty()) { // Only load if form is currently empty (initial load)
                title = info.book.title
                readingStatus = info.book.readingStatus
                description = info.book.description ?: ""
                rating = info.book.rating ?: 0f
                selectedAuthors = info.authors
                selectedGenres = info.genres
                selectedTags = info.tags
                links = info.links
            }
        }
    }

    // Pre-fill logic (Share Intent)
    LaunchedEffect(initialBookInfo) {
        initialBookInfo?.let { info ->
            title = info.title
            description = info.description
            
            // Map strings to objects
            // Note: Since IDs are 0, repository will handle creation or matching by name if implemented
            selectedAuthors = info.authors.map { Author(name = it) }
            selectedGenres = info.genres.map { Genre(name = it) }
            
            if (info.sourceUrl.isNotBlank()) {
                links = listOf(BookLink(bookId = 0, linkText = "Source", url = info.sourceUrl))
            }

            scope.launch {
                snackbarHostState.showSnackbar("Information pre-filled from web page.")
            }
        }
    }

    // Duplicate Check State
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateBook by remember { mutableStateOf<Book?>(null) }

    fun saveBook() {
        if (bookId == null) {
            viewModel.addBook(
                title = title,
                readingStatus = readingStatus,
                rating = if (rating > 0) rating else null,
                description = description,
                authors = selectedAuthors,
                genres = selectedGenres,
                tags = selectedTags,
                links = links
            )
        } else {
            viewModel.updateBook(
                id = bookId,
                title = title,
                readingStatus = readingStatus,
                rating = if (rating > 0) rating else null,
                description = description,
                authors = selectedAuthors,
                genres = selectedGenres,
                tags = selectedTags,
                links = links
            )
        }
        onNavigateBack()
    }

    fun checkAndSave() {
        if (title.isBlank()) return // TODO: Show error

        scope.launch {
            val duplicate = viewModel.checkDuplicateBook(title)
            if (duplicate != null) {
                duplicateBook = duplicate
                showDuplicateDialog = true
            } else {
                saveBook()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (bookId == null) "Add Book" else "Edit Book") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Reading Status (Simple Dropdown or Radio for now, let's use Dropdown)
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = readingStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    listOf("想讀", "閱讀中", "已讀完").forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                readingStatus = status
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Authors
            AutocompleteTagInput(
                label = "Authors",
                items = allAuthors,
                selectedItems = selectedAuthors,
                onItemAdded = { author -> selectedAuthors = selectedAuthors + author },
                onItemRemoved = { author -> selectedAuthors = selectedAuthors - author },
                itemToString = { it.name },
                stringToItem = { Author(name = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Genres
            AutocompleteTagInput(
                label = "Genres",
                items = allGenres,
                selectedItems = selectedGenres,
                onItemAdded = { genre -> selectedGenres = selectedGenres + genre },
                onItemRemoved = { genre -> selectedGenres = selectedGenres - genre },
                itemToString = { it.name },
                stringToItem = { Genre(name = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            AutocompleteTagInput(
                label = "Tags",
                items = allTags,
                selectedItems = selectedTags,
                onItemAdded = { tag -> selectedTags = selectedTags + tag },
                onItemRemoved = { tag -> selectedTags = selectedTags - tag },
                itemToString = { it.name },
                stringToItem = { Tag(name = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Links
            DynamicLinkList(
                links = links,
                onLinkAdded = { links = links + BookLink(bookId = 0, linkText = "", url = "") }, // bookId ignored on insert
                onLinkRemoved = { link -> links = links - link },
                onLinkUpdated = { index, newLink -> 
                     val newLinks = links.toMutableList()
                     newLinks[index] = newLink
                     links = newLinks
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { checkAndSave() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }

    if (showDuplicateDialog && duplicateBook != null) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate Book Found") },
            text = { 
                Text("A book named '${duplicateBook?.title}' already exists.\n" +
                     "Status: ${duplicateBook?.readingStatus}\n" +
                     "Is this the same book?") 
            },
            confirmButton = {
                TextButton(onClick = {
                    showDuplicateDialog = false
                    saveBook()
                }) {
                    Text("No, Create New")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDuplicateDialog = false
                    onNavigateToDetail(duplicateBook!!.id)
                }) {
                    Text("Yes, View Existing")
                }
            }
        )
    }
}
