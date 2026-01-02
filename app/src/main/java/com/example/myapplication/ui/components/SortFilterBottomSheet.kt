package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.repository.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterBottomSheet(
    onDismissRequest: () -> Unit,
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    currentReadingStatus: String?,
    onReadingStatusSelected: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Sort By", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SortOrder.values()) { sortOrder ->
                    FilterChip(
                        selected = sortOrder == currentSortOrder,
                        onClick = { onSortOrderSelected(sortOrder) },
                        label = { Text(getSortLabel(sortOrder)) },
                        leadingIcon = if (sortOrder == currentSortOrder) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Filter By Status", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "想讀", "閱讀中", "已讀完").forEach { status ->
                    val isSelected = (status == "All" && currentReadingStatus == null) || status == currentReadingStatus
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            if (status == "All") onReadingStatusSelected(null) 
                            else onReadingStatusSelected(status)
                        },
                        label = { Text(status) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onClearFilters()
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Filters")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun getSortLabel(sortOrder: SortOrder): String {
    return when (sortOrder) {
        SortOrder.TITLE_ASC -> "Title (A-Z)"
        SortOrder.TITLE_DESC -> "Title (Z-A)"
        SortOrder.RATING_ASC -> "Rating (Low)"
        SortOrder.RATING_DESC -> "Rating (High)"
        SortOrder.DATE_ADDED_ASC -> "Oldest"
        SortOrder.DATE_ADDED_DESC -> "Newest"
        SortOrder.DATE_MODIFIED_ASC -> "Modified (Old)"
        SortOrder.DATE_MODIFIED_DESC -> "Modified (New)"
    }
}
