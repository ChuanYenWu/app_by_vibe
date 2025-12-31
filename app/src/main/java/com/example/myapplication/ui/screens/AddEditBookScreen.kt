package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.entity.*
import com.example.myapplication.ui.components.AutocompleteTagInput
import com.example.myapplication.ui.components.DynamicLinkList
import com.example.myapplication.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
) {
    val scope = rememberCoroutineScope()
    
    // Data from ViewModel
    val allAuthors by viewModel.allAuthors.collectAsState()
    val allGenres by viewModel.allGenres.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    // Form State
    var title by remember { mutableStateOf("") }
    var readingStatus by remember { mutableStateOf("想讀") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) } // Simple Float implementation

    // Selection State
    var selectedAuthors by remember { mutableStateOf(listOf<Author>()) }
    var selectedGenres by remember { mutableStateOf(listOf<Genre>()) }
    var selectedTags by remember { mutableStateOf(listOf<Tag>()) }
    var links by remember { mutableStateOf(listOf<BookLink>()) }

    // Duplicate Check State
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateBook by remember { mutableStateOf<Book?>(null) }

    fun saveBook() {
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
        topBar = { TopAppBar(title = { Text("Add Book") }) }
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
