// app/src/main/java/com/py/ani_nderesarai/ui/screens/AddEditReminderScreen.kt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.py.ani_nderesarai.data.model.*
import com.py.ani_nderesarai.ui.components.*
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
    var isRecurring by remember { mutableStateOf(false) }
    var recurringType by remember { mutableStateOf(RecurringType.MONTHLY) }
    var whatsappNumber by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var selectedIcon by remember { mutableStateOf(PaymentIconType.GENERIC) }
    var notes by remember { mutableStateOf("") }
    
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
            whatsappNumber = reminder.whatsappNumber
            customMessage = reminder.customMessage
            priority = reminder.priority
            selectedColor = reminder.color
            selectedIcon = reminder.iconType
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = if (uiState.isEditMode) "Editar Recordatorio" else "Nuevo Recordatorio",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(
                onClick = {
                    // Validar campos requeridos
                    if (title.isBlank()) {
                        return@TextButton
                    }
                    
                    // Guardar recordatorio
                    viewModel.saveReminder(
                        PaymentReminder(
                            title = title,
                            description = description,
                            category = category,
                            amount = amount.toDoubleOrNull(),
                            dueDate = dueDate,
                            reminderTime = reminderTime,
                            isRecurring = isRecurring,
                            recurringType = recurringType,
                            whatsappNumber = whatsappNumber,
                            customMessage = customMessage,
                            priority = priority,
                            color = selectedColor,
                            iconType = selectedIcon,
                            notes = notes
                        )
                    )
                }
            ) {
                Text("Guardar")
            }
        }
        
        // Información básica
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Información Básica",
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del recordatorio *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank(),
                    supportingText = if (title.isBlank()) {
                        { Text("El título es obligatorio") }
                    } else null
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                CategorySelector(
                    selectedCategory = category,
                    onCategorySelected = { category = it }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            // Solo permitir números y punto decimal
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amount = it
                            }
                        },
                        label = { Text("Monto") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ej: 150000") }
                    )
                    OutlinedTextField(
                        value = "PYG",
                        onValueChange = { },
                        label = { Text("Moneda") },
                        modifier = Modifier.weight(0.5f),
                        enabled = false
                    )
                }
            }
        }
        
        // Fecha y hora
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Fecha y Hora",
                    style = MaterialTheme.typography.titleMedium
                )
                
                DatePickerField(
                    selectedDate = dueDate,
                    onDateSelected = { dueDate = it },
                    label = "Fecha de vencimiento"
                )
                
                TimePickerField(
                    selectedTime = reminderTime,
                    onTimeSelected = { reminderTime = it },
                    label = "Hora del recordatorio"
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recordatorio recurrente")
                }
                
                if (isRecurring) {
                    RecurringTypeSelector(
                        selectedType = recurringType,
                        onTypeSelected = { recurringType = it }
                    )
                }
            }
        }
        
        // WhatsApp y notificaciones
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Notificaciones WhatsApp",
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("Número de WhatsApp") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: +595981234567") }
                )
                
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Mensaje personalizado") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { Text("Mensaje que se enviará por WhatsApp") }
                )
            }
        }
        
        // Personalización
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Personalización",
                    style = MaterialTheme.typography.titleMedium
                )
                
                PrioritySelector(
                    selectedPriority = priority,
                    onPrioritySelected = { priority = it }
                )
                
                ColorSelector(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
                
                IconSelector(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it }
                )
            }
        }
        
        // Notas adicionales
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Notas adicionales",
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    placeholder = { Text("Información adicional sobre este pago...") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Mostrar mensaje de error si existe
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
