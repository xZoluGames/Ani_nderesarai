package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.Priority

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Prioridad",
            style = MaterialTheme.typography.labelMedium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Priority.values()) { priority ->
                PriorityChip(
                    priority = priority,
                    isSelected = priority == selectedPriority,
                    onClick = { onPrioritySelected(priority) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityChip(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = getPriorityIcon(priority),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = getPriorityColor(priority)
                )
                Text(
                    text = getPriorityName(priority),
                    color = getPriorityColor(priority)
                )
            }
        },
        selected = isSelected
    )
}

fun getPriorityIcon(priority: Priority): ImageVector {
    return when (priority) {
        Priority.LOW -> Icons.Default.KeyboardArrowDown
        Priority.MEDIUM -> Icons.Default.Remove
        Priority.HIGH -> Icons.Default.KeyboardArrowUp
        Priority.URGENT -> Icons.Default.PriorityHigh
    }
}

fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.MEDIUM -> Color(0xFF2196F3)
        Priority.HIGH -> Color(0xFFFF9800)
        Priority.URGENT -> Color(0xFFF44336)
    }
}

fun getPriorityName(priority: Priority): String {
    return when (priority) {
        Priority.LOW -> "Baja"
        Priority.MEDIUM -> "Media"
        Priority.HIGH -> "Alta"
        Priority.URGENT -> "Urgente"
    }
}