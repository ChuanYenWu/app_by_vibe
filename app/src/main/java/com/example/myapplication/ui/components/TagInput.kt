package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> AutocompleteTagInput(
    label: String,
    items: List<T>,
    selectedItems: List<T>,
    onItemAdded: (T) -> Unit,
    onItemRemoved: (T) -> Unit,
    itemToString: (T) -> String,
    stringToItem: (String) -> T, // Factory to create new item from string
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredItems = items.filter { 
        itemToString(it).contains(query, ignoreCase = true) && !selectedItems.contains(it)
    }

    Column(modifier = modifier) {
        // Selected Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedItems.forEach { item ->
                InputChip(
                    selected = true,
                    onClick = { /* No op */ },
                    label = { Text(itemToString(item)) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.clickable { onItemRemoved(item) }
                        )
                    }
                )
            }
        }

        // Input Field
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { 
                    query = it
                    expanded = true
                },
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (query.isNotBlank()) {
                            onItemAdded(stringToItem(query))
                            query = ""
                            expanded = false
                        }
                    }
                )
            )

            if (filteredItems.isNotEmpty() && query.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemToString(item)) },
                            onClick = {
                                onItemAdded(item)
                                query = ""
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
