package com.py.ani_nderesarai.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.py.ani_nderesarai.data.model.Priority

/**
 * Utilidades para gestión de prioridades
 * Centraliza funciones compartidas entre componentes
 */

/**
 * Obtiene el ícono correspondiente a una prioridad
 */
fun getPriorityIcon(priority: Priority): ImageVector {
    return when (priority) {
        Priority.LOW -> Icons.Default.KeyboardArrowDown
        Priority.MEDIUM -> Icons.Default.Remove
        Priority.HIGH -> Icons.Default.KeyboardArrowUp
        Priority.URGENT -> Icons.Default.PriorityHigh
    }
}

/**
 * Obtiene el color correspondiente a una prioridad
 */
@Composable
fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.LOW -> MaterialTheme.colorScheme.tertiary
        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        Priority.HIGH -> MaterialTheme.colorScheme.secondary
        Priority.URGENT -> MaterialTheme.colorScheme.error
    }
}

/**
 * Obtiene la etiqueta en español de una prioridad
 */
fun getPriorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.LOW -> "Baja"
        Priority.MEDIUM -> "Media"
        Priority.HIGH -> "Alta"
        Priority.URGENT -> "Urgente"
    }
}