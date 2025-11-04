package com.py.ani_nderesarai.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: PaymentReminder,
    isOverdue: Boolean = false,
    isPaid: Boolean = false,
    isCancelled: Boolean = false,
    onEdit: () -> Unit = {},
    onMarkAsPaid: () -> Unit = {},
    onCancel: () -> Unit = {},
    onDelete: () -> Unit = {},
    onSendWhatsApp: () -> Unit = {},
    onReactivate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Calcular días hasta vencimiento
    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), reminder.dueDate)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Determinar colores según estado
    val (containerColor, contentColor, borderColor) = when {
        isCancelled -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        isPaid -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
        isOverdue -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )
        daysUntil <= 1 -> Triple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
        daysUntil <= 3 -> Triple(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
        else -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Título y categoría
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(getCategoryEmoji(reminder.category))

                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            textDecoration = if (isPaid || isCancelled)
                                TextDecoration.LineThrough
                            else
                                null
                        )
                    }

                    if (reminder.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }

                // Menú de opciones
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        when {
                            isCancelled -> {
                                // Opciones para cancelados
                                DropdownMenuItem(
                                    text = { Text("Reactivar") },
                                    onClick = {
                                        onReactivate()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar permanentemente") },
                                    onClick = {
                                        onDelete()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                            isPaid -> {
                                // Opciones para pagados
                                DropdownMenuItem(
                                    text = { Text("Ver detalles") },
                                    onClick = {
                                        onEdit()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Visibility, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        onDelete()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                            else -> {
                                // Opciones para activos
                                DropdownMenuItem(
                                    text = { Text("Marcar como pagado") },
                                    onClick = {
                                        onMarkAsPaid()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Enviar por WhatsApp") },
                                    onClick = {
                                        onSendWhatsApp()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Send, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        onEdit()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Cancelar deuda") },
                                    onClick = {
                                        onCancel()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = contentColor
                        )
                        Text(
                            text = reminder.dueDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                    }

                    // Días restantes
                    when {
                        isPaid -> {
                            reminder.lastPaid?.let { paidDate ->
                                Text(
                                    text = "Pagado: ${paidDate.format(dateFormatter)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                        isCancelled -> {
                            Text(
                                text = "Cancelado",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                        isOverdue -> {
                            Text(
                                text = "⚠️ Vencido hace ${-daysUntil} día(s)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        daysUntil == 0L -> {
                            Text(
                                text = "⚠️ ¡Vence HOY!",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        daysUntil == 1L -> {
                            Text(
                                text = "⏰ Vence MAÑANA",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        else -> {
                            Text(
                                text = "En $daysUntil día(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Monto
                reminder.amount?.let { amount ->
                    Text(
                        text = formatCurrency(amount, reminder.currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }

            // Info de cuotas
            if (reminder.isInstallments) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = contentColor
                        )
                        Text(
                            text = "Cuota ${reminder.currentInstallment} de ${reminder.totalInstallments}",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor
                        )

                        // Barra de progreso
                        LinearProgressIndicator(
                            progress = reminder.currentInstallment.toFloat() / reminder.totalInstallments.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Prioridad y tags
            if (reminder.priority != Priority.MEDIUM || reminder.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prioridad
                    if (reminder.priority != Priority.MEDIUM) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    getPriorityLabel(reminder.priority),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    getPriorityIcon(reminder.priority),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (reminder.priority) {
                                    Priority.URGENT -> MaterialTheme.colorScheme.errorContainer
                                    Priority.HIGH -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )
                    }

                    // Tags
                    reminder.tags.take(2).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }

            // Botones de acción rápida (solo para activos)
            if (!isPaid && !isCancelled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onMarkAsPaid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pagado")
                    }

                    OutlinedButton(
                        onClick = onSendWhatsApp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enviar")
                    }
                }
            }

            // Botón de reactivar (solo para cancelados)
            if (isCancelled) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onReactivate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reactivar recordatorio")
                }
            }
        }
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================

private fun getCategoryEmoji(category: PaymentCategory): String {
    return when (category) {
        PaymentCategory.UTILITIES -> "🏠"
        PaymentCategory.WATER -> "💧"
        PaymentCategory.ELECTRICITY -> "⚡"
        PaymentCategory.GAS -> "🔥"
        PaymentCategory.INTERNET -> "🌐"
        PaymentCategory.PHONE -> "📱"
        PaymentCategory.LOANS -> "🏦"
        PaymentCategory.CREDIT_CARDS -> "💳"
        PaymentCategory.INSURANCE -> "🛡️"
        PaymentCategory.RENT -> "🏘️"
        PaymentCategory.SUBSCRIPTIONS -> "📺"
        PaymentCategory.TAXES -> "💼"
        PaymentCategory.EDUCATION -> "📚"
        PaymentCategory.HEALTH -> "🏥"
        PaymentCategory.ENTERTAINMENT -> "🎮"
        PaymentCategory.TRANSPORT -> "🚗"
        PaymentCategory.OTHER -> "📋"
    }
}

private fun getPriorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.LOW -> "Baja"
        Priority.MEDIUM -> "Media"
        Priority.HIGH -> "Alta"
        Priority.URGENT -> "Urgente"
    }
}

private fun getPriorityIcon(priority: Priority): androidx.compose.ui.graphics.vector.ImageVector {
    return when (priority) {
        Priority.LOW -> Icons.Default.KeyboardArrowDown
        Priority.MEDIUM -> Icons.Default.Remove
        Priority.HIGH -> Icons.Default.KeyboardArrowUp
        Priority.URGENT -> Icons.Default.PriorityHigh
    }
}

private fun formatCurrency(amount: Double, currency: String): String {
    return when (currency) {
        "PYG" -> "₲ ${String.format("%,.0f", amount)}"
        "USD" -> "$ ${String.format("%.2f", amount)}"
        "EUR" -> "€ ${String.format("%.2f", amount)}"
        "BRL" -> "R$ ${String.format("%.2f", amount)}"
        else -> "$currency ${String.format("%.2f", amount)}"
    }
}