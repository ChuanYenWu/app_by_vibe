package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.entity.BookLink
import com.example.myapplication.ui.viewmodel.BookViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)
) {
    val uriHandler = LocalUriHandler.current
    val bookWithInfo by viewModel.getBookById(bookId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(bookId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (bookWithInfo != null) {
            val book = bookWithInfo!!.book
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // Title & Status
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(book.readingStatus) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (book.rating != null) {
                        Text(text = "Rating: ${book.rating}/5.0")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Authors
                Text("Authors", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bookWithInfo!!.authors.forEach { author ->
                        AssistChip(
                            onClick = {},
                            label = { Text(author.name) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (!book.description.isNullOrBlank()) {
                    Text("Description", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Genres
                if (bookWithInfo!!.genres.isNotEmpty()) {
                    Text("Genres", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        bookWithInfo!!.genres.forEach { genre ->
                             AssistChip(
                                onClick = {},
                                label = { Text(genre.name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tags
                if (bookWithInfo!!.tags.isNotEmpty()) {
                    Text("Tags", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        bookWithInfo!!.tags.forEach { tag ->
                             AssistChip(
                                onClick = {},
                                label = { Text(tag.name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Links
                if (bookWithInfo!!.links.isNotEmpty()) {
                    Text("Links", style = MaterialTheme.typography.titleMedium)
                    bookWithInfo!!.links.forEach { link ->
                         Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(vertical = 4.dp)
                                 .clickable { 
                                     if (link.url.isNotBlank()) {
                                         try {
                                             uriHandler.openUri(link.url)
                                         } catch (e: Exception) {
                                             // Silent fail
                                         }
                                     }
                                 }
                         ) {
                             Text(text = link.linkText.ifBlank { link.url }, color = MaterialTheme.colorScheme.primary)
                         }
                    }
                }
            }
        } else {
            // Loading or Error
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
