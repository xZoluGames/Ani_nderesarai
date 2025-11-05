package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.*
import java.time.LocalDate
import com.py.ani_nderesarai.ui.utils.getPriorityIcon
import com.py.ani_nderesarai.ui.utils.getPriorityColor
import com.py.ani_nderesarai.ui.components.getCategoryLabel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (ReminderFilters) -> Unit,
    onClearFilters: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf<ReminderStatus?>(null) }
    var selectedCategory by remember { mutableStateOf<PaymentCategory?>(null) }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }

    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }

    var dateFrom by remember { mutableStateOf<LocalDate?>(null) }
    var dateTo by remember { mutableStateOf<LocalDate?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    var showStatusMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔍 Filtros Avanzados")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ============================================
                // BÚSQUEDA POR TEXTO
                // ============================================
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por título o descripción") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                HorizontalDivider()

                // ============================================
                // FILTRO POR ESTADO
                // ============================================
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Box {
                    OutlinedButton(
                        onClick = { showStatusMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedStatus?.name ?: "Todos los estados")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                selectedStatus = null
                                showStatusMenu = false
                            }
                        )
                        ReminderStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(getStatusLabel(status)) },
                                onClick = {
                                    selectedStatus = status
                                    showStatusMenu = false
                                },
                                leadingIcon = {
                                    Icon(getStatusIcon(status), contentDescription = null)
                                }
                            )
                        }
                    }
                }

                // ============================================
                // FILTRO POR CATEGORÍA
                // ============================================
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Box {
                    OutlinedButton(
                        onClick = { showCategoryMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCategory?.let { getCategoryLabel(it) } ?: "Todas las categorías")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false },
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = {
                                selectedCategory = null
                                showCategoryMenu = false
                            }
                        )
                        PaymentCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(getCategoryLabel(category)) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryMenu = false
                                },
                                leadingIcon = {
                                    Text(getCategoryEmoji(category))
                                }
                            )
                        }
                    }
                }

                // ============================================
                // FILTRO POR PRIORIDAD
                // ============================================
                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Box {
                    OutlinedButton(
                        onClick = { showPriorityMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedPriority?.let { getPriorityLabel(it) } ?: "Todas las prioridades")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showPriorityMenu,
                        onDismissRequest = { showPriorityMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = {
                                selectedPriority = null
                                showPriorityMenu = false
                            }
                        )
                        Priority.values().forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(getPriorityLabel(priority)) },
                                onClick = {
                                    selectedPriority = priority
                                    showPriorityMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        getPriorityIcon(priority),
                                        contentDescription = null,
                                        tint = getPriorityColor(priority)
                                    )
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // ============================================
                // FILTRO POR MONTO
                // ============================================
                Text(
                    text = "Rango de Monto",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                minAmount = it
                            }
                        },
                        label = { Text("Mínimo") },
                        leadingIcon = { Text("₲") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = maxAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                maxAmount = it
                            }
                        },
                        label = { Text("Máximo") },
                        leadingIcon = { Text("₲") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider()

                // ============================================
                // FILTRO POR FECHAS
                // ============================================
                Text(
                    text = "Rango de Fechas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fecha desde
                    DatePickerField(
                        selectedDate = dateFrom,
                        onDateSelected = { dateFrom = it },
                        label = "Desde",
                        modifier = Modifier.weight(1f)
                    )

                    // Fecha hasta
                    DatePickerField(
                        selectedDate = dateTo,
                        onDateSelected = { dateTo = it },
                        label = "Hasta",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Botones de fecha rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            dateFrom = LocalDate.now()
                            dateTo = LocalDate.now().plusWeeks(1)
                        },
                        label = { Text("Esta semana", style = MaterialTheme.typography.bodySmall) }
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            dateFrom = LocalDate.now()
                            dateTo = LocalDate.now().plusMonths(1)
                        },
                        label = { Text("Este mes", style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filters = ReminderFilters(
                        status = selectedStatus,
                        category = selectedCategory,
                        priority = selectedPriority,
                        minAmount = minAmount.toDoubleOrNull(),
                        maxAmount = maxAmount.toDoubleOrNull(),
                        dateFrom = dateFrom,
                        dateTo = dateTo,
                        searchQuery = searchQuery
                    )
                    onApplyFilters(filters)
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClearFilters()
            }) {
                Text("Limpiar filtros")
            }
        }
    )
}

// ============================================
// HELPER FUNCTIONS
// ============================================

private fun getStatusLabel(status: ReminderStatus): String {
    return when (status) {
        ReminderStatus.ACTIVE -> "Activo"
        ReminderStatus.PAID -> "Pagado"
        ReminderStatus.CANCELLED -> "Cancelado"
        ReminderStatus.OVERDUE -> "Vencido"
        ReminderStatus.PARTIAL -> "Parcial"
    }
}

private fun getStatusIcon(status: ReminderStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        ReminderStatus.ACTIVE -> Icons.Default.Notifications
        ReminderStatus.PAID -> Icons.Default.CheckCircle
        ReminderStatus.CANCELLED -> Icons.Default.Cancel
        ReminderStatus.OVERDUE -> Icons.Default.Warning
        ReminderStatus.PARTIAL -> Icons.Default.HourglassEmpty
    }
}


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


// ============================================
// DATE PICKER FIELD
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = modifier
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = selectedDate?.toString() ?: "Sin fecha",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(java.time.ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    showDatePicker = false
                }) {
                    Text("Limpiar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}