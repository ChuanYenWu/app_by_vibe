package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EntityListScreen(
    title: String,
    items: List<T>,
    onEdit: (T, String) -> Unit,
    onDelete: (T) -> Unit,
    nameProvider: (T) -> String
) {
    var itemToEdit by remember { mutableStateOf<T?>(null) }
    var itemToDelete by remember { mutableStateOf<T?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(items) { item ->
                ListItem(
                    headlineContent = { Text(nameProvider(item)) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { itemToEdit = item }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { itemToDelete = item }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (itemToEdit != null) {
        var newName by remember { mutableStateOf(nameProvider(itemToEdit!!)) }
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit ${nameProvider(itemToEdit!!)}") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(itemToEdit!!, newName)
                    itemToEdit = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete ${nameProvider(itemToDelete!!)}?") },
            text = { Text("This will remove the item from all associated books.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(itemToDelete!!)
                    itemToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
