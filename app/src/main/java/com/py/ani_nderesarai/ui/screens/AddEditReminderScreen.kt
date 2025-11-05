package com.py.ani_nderesarai.ui.screens

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.py.ani_nderesarai.data.model.*
import com.py.ani_nderesarai.ui.components.DatePickerField
import com.py.ani_nderesarai.ui.components.TimePickerField
import com.py.ani_nderesarai.ui.viewmodel.AddEditReminderViewModel
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    reminderId: Long = 0L,
    onNavigateBack: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estados del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PaymentCategory.UTILITIES) }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var reminderTime by remember { mutableStateOf(LocalTime.of(9, 0)) }

    // Recurrencia
    var isRecurring by remember { mutableStateOf(false) }
    var recurringType by remember { mutableStateOf(RecurringType.MONTHLY) }
    var customRecurringDays by remember { mutableStateOf("30") }

    // ✅ NUEVO: Sistema de cuotas
    var isInstallments by remember { mutableStateOf(false) }
    var totalInstallments by remember { mutableStateOf("1") }
    var installmentInterval by remember { mutableStateOf("30") }

    // WhatsApp y otros
    var whatsappNumber by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var notes by remember { mutableStateOf("") }

    // UI State
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showRecurringTypeMenu by remember { mutableStateOf(false) }

    // Cargar datos si es modo edición
    LaunchedEffect(uiState.reminder) {
        uiState.reminder?.let { reminder ->
            title = reminder.title
            description = reminder.description
            category = reminder.category
            amount = reminder.amount?.toString() ?: ""
            dueDate = reminder.dueDate
            reminderTime = reminder.reminderTime
            isRecurring = reminder.isRecurring
            recurringType = reminder.recurringType
            customRecurringDays = reminder.customRecurringDays.toString()
            isInstallments = reminder.isInstallments
            totalInstallments = reminder.totalInstallments.toString()
            installmentInterval = reminder.installmentInterval.toString()
            whatsappNumber = reminder.whatsappNumber
            customMessage = reminder.customMessage
            priority = reminder.priority
            notes = reminder.notes
        }
    }

    // Navegar atrás cuando se guarde exitosamente
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Editar Recordatorio" else "Nuevo Recordatorio")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Validar campos requeridos
                            if (title.isBlank()) {
                                return@TextButton
                            }

                            // Guardar recordatorio
                            viewModel.saveReminder(
                                PaymentReminder(
                                    id = if (uiState.isEditMode) uiState.reminder?.id ?: 0 else 0,
                                    title = title,
                                    description = description,
                                    category = category,
                                    amount = amount.toDoubleOrNull(),
                                    dueDate = dueDate,
                                    reminderTime = reminderTime,
                                    isRecurring = isRecurring,
                                    recurringType = recurringType,
                                    customRecurringDays = customRecurringDays.toIntOrNull() ?: 30,
                                    isInstallments = isInstallments,
                                    totalInstallments = totalInstallments.toIntOrNull() ?: 1,
                                    currentInstallment = 1,
                                    installmentInterval = installmentInterval.toIntOrNull() ?: 30,
                                    whatsappNumber = whatsappNumber,
                                    customMessage = customMessage,
                                    priority = priority,
                                    notes = notes
                                )
                            )
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ============================================
            // INFORMACIÓN BÁSICA
            // ============================================
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Selector de categoría
                    Box {
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(getCategoryEmoji(category))
                            Spacer(Modifier.width(8.dp))
                            Text(getCategoryLabel(category))
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.heightIn(max = 400.dp)
                        ) {
                            PaymentCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(getCategoryEmoji(cat))
                                            Spacer(Modifier.width(8.dp))
                                            Text(getCategoryLabel(cat))
                                        }
                                    },
                                    onClick = {
                                        category = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Monto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    amount = it
                                }
                            },
                            label = { Text("Monto") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Text("₲") },
                            placeholder = { Text("Ej: 150000") }
                        )
                    }
                }
            }

            // ============================================
            // FECHA Y HORA
            // ============================================
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Fecha y Hora",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DatePickerField(
                        selectedDate = dueDate,
                        onDateSelected = { date: LocalDate? -> dueDate = date },
                        label = "Fecha de vencimiento",
                        modifier = Modifier // Esto obliga a usar la versión con `Modifier`
                    )

                    TimePickerField(
                        selectedTime = reminderTime,
                        onTimeSelected = { reminderTime = it },
                        label = "Hora del recordatorio"
                    )
                }
            }

            // ============================================
            // ✅ NUEVO: SISTEMA DE CUOTAS
            // ============================================
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pago en Cuotas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Para préstamos o pagos divididos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isInstallments,
                            onCheckedChange = {
                                isInstallments = it
                                if (it) {
                                    isRecurring = false // No puede ser ambos
                                }
                            }
                        )
                    }

                    if (isInstallments) {
                        HorizontalDivider()

                        Text(
                            text = "Ejemplo: Préstamo en 10 cuotas cada 7 días",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = totalInstallments,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                        totalInstallments = it
                                    }
                                },
                                label = { Text("Total de cuotas") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                                placeholder = { Text("10") }
                            )

                            OutlinedTextField(
                                value = installmentInterval,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                        installmentInterval = it
                                    }
                                },
                                label = { Text("Cada X días") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                placeholder = { Text("30") }
                            )
                        }

                        // Preview de cuotas
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💡 Vista previa",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                val installmentsCount = totalInstallments.toIntOrNull() ?: 1
                                val interval = installmentInterval.toIntOrNull() ?: 30
                                val amountPerInstallment = amount.toDoubleOrNull()?.div(installmentsCount)

                                Text(
                                    text = "• $installmentsCount cuotas de ${amountPerInstallment?.let { formatCurrency(it) } ?: "₲ ?"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• Pago cada $interval días",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // ============================================
            // RECURRENCIA
            // ============================================
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recordatorio Recurrente",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Para pagos que se repiten",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = {
                                isRecurring = it
                                if (it) {
                                    isInstallments = false // No puede ser ambos
                                }
                            }
                        )
                    }

                    if (isRecurring) {
                        HorizontalDivider()

                        // Selector de tipo de recurrencia
                        Box {
                            OutlinedButton(
                                onClick = { showRecurringTypeMenu = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(getRecurringTypeLabel(recurringType))
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = showRecurringTypeMenu,
                                onDismissRequest = { showRecurringTypeMenu = false }
                            ) {
                                RecurringType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(getRecurringTypeLabel(type)) },
                                        onClick = {
                                            recurringType = type
                                            showRecurringTypeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Campo personalizado si es CUSTOM
                        if (recurringType == RecurringType.CUSTOM) {
                            OutlinedTextField(
                                value = customRecurringDays,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                        customRecurringDays = it
                                    }
                                },
                                label = { Text("Cada cuántos días") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                placeholder = { Text("Ej: 45 para cada 45 días") }
                            )
                        }
                    }
                }
            }

// CONTINUACIÓN de AddEditReminderScreen_v2.kt
// Agregar estas secciones después de la sección de Recurrencia

            // ============================================
            // WHATSAPP
            // ============================================
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "WhatsApp",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("Número de WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        placeholder = { Text("595981234567") },
                        supportingText = {
                            Text("Opcional. Para enviar este recordatorio específico a otro número")
                        }
                    )

                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        label = { Text("Mensaje personalizado") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        placeholder = { Text("Escribe un mensaje personalizado para este pago...") },
                        supportingText = {
                            Text("Opcional. Se enviará junto con los detalles del pago")
                        }
                    )
                }
            }

            // ============================================
            // PRIORIDAD Y NOTAS
            // ============================================
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Opciones Adicionales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Selector de prioridad
                    Text(
                        text = "Prioridad",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showPriorityMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                getPriorityIcon(priority),
                                contentDescription = null,
                                tint = getPriorityColor(priority)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(getPriorityLabel(priority))
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showPriorityMenu,
                            onDismissRequest = { showPriorityMenu = false }
                        ) {
                            Priority.values().forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(getPriorityLabel(p)) },
                                    onClick = {
                                        priority = p
                                        showPriorityMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            getPriorityIcon(p),
                                            contentDescription = null,
                                            tint = getPriorityColor(p)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Notas
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        placeholder = { Text("Información adicional sobre este pago...") }
                    )
                }
            }

            // ============================================
            // BOTÓN GUARDAR (ALTERNATIVO AL TOPBAR)
            // ============================================
            Button(
                onClick = {
                    if (title.isBlank()) {
                        return@Button
                    }

                    viewModel.saveReminder(
                        PaymentReminder(
                            id = if (uiState.isEditMode) uiState.reminder?.id ?: 0 else 0,
                            title = title,
                            description = description,
                            category = category,
                            amount = amount.toDoubleOrNull(),
                            dueDate = dueDate,
                            reminderTime = reminderTime,
                            isRecurring = isRecurring,
                            recurringType = recurringType,
                            customRecurringDays = customRecurringDays.toIntOrNull() ?: 30,
                            isInstallments = isInstallments,
                            totalInstallments = totalInstallments.toIntOrNull() ?: 1,
                            currentInstallment = 1,
                            installmentInterval = installmentInterval.toIntOrNull() ?: 30,
                            whatsappNumber = whatsappNumber,
                            customMessage = customMessage,
                            priority = priority,
                            notes = notes
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Recordatorio", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }

        // Mostrar errores
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================

private fun getCategoryLabel(category: PaymentCategory): String {
    return when (category) {
        PaymentCategory.UTILITIES -> "Servicios"
        PaymentCategory.WATER -> "Agua"
        PaymentCategory.ELECTRICITY -> "Electricidad"
        PaymentCategory.GAS -> "Gas"
        PaymentCategory.INTERNET -> "Internet"
        PaymentCategory.PHONE -> "Teléfono"
        PaymentCategory.LOANS -> "Préstamos"
        PaymentCategory.CREDIT_CARDS -> "Tarjetas de Crédito"
        PaymentCategory.INSURANCE -> "Seguros"
        PaymentCategory.RENT -> "Alquiler"
        PaymentCategory.SUBSCRIPTIONS -> "Suscripciones"
        PaymentCategory.TAXES -> "Impuestos"
        PaymentCategory.EDUCATION -> "Educación"
        PaymentCategory.HEALTH -> "Salud"
        PaymentCategory.ENTERTAINMENT -> "Entretenimiento"
        PaymentCategory.TRANSPORT -> "Transporte"
        PaymentCategory.OTHER -> "Otros"
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

private fun getRecurringTypeLabel(type: RecurringType): String {
    return when (type) {
        RecurringType.DAILY -> "Diario"
        RecurringType.WEEKLY -> "Semanal"
        RecurringType.BIWEEKLY -> "Quincenal"
        RecurringType.MONTHLY -> "Mensual"
        RecurringType.BIMONTHLY -> "Bimestral (cada 2 meses)"
        RecurringType.QUARTERLY -> "Trimestral (cada 3 meses)"
        RecurringType.SEMI_ANNUAL -> "Semestral (cada 6 meses)"
        RecurringType.ANNUAL -> "Anual"
        RecurringType.CUSTOM -> "Personalizado"
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

@Composable
private fun getPriorityColor(priority: Priority): androidx.compose.ui.graphics.Color {
    return when (priority) {
        Priority.LOW -> MaterialTheme.colorScheme.tertiary
        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        Priority.HIGH -> MaterialTheme.colorScheme.secondary
        Priority.URGENT -> MaterialTheme.colorScheme.error
    }
}

private fun formatCurrency(amount: Double): String {
    return "₲ ${String.format("%,.0f", amount)}"
}

// ============================================
// TIME PICKER COMPONENT
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute
    )

    OutlinedButton(
        onClick = { showTimePicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.AccessTime, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = String.format("%02d:%02d", selectedTime.hour, selectedTime.minute),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(label) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(
                        LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}




// Helper functions siguen en el siguiente archivo...