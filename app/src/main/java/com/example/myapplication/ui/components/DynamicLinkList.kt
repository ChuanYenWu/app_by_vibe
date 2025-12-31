package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.BookLink

@Composable
fun DynamicLinkList(
    links: List<BookLink>,
    onLinkAdded: () -> Unit,
    onLinkRemoved: (BookLink) -> Unit,
    onLinkUpdated: (Int, BookLink) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Links", style = MaterialTheme.typography.titleMedium)
        
        links.forEachIndexed { index, link ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = link.linkText,
                    onValueChange = { onLinkUpdated(index, link.copy(linkText = it)) },
                    label = { Text("Text") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = link.url,
                    onValueChange = { onLinkUpdated(index, link.copy(url = it)) },
                    label = { Text("URL") },
                    modifier = Modifier.weight(2f)
                )
                IconButton(onClick = { onLinkRemoved(link) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Link")
                }
            }
        }

        OutlinedButton(
            onClick = onLinkAdded,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Link")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Link")
        }
    }
}
