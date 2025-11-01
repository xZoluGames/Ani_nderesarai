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
    onNavigateBack: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PaymentCategory.UTILITIES) }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var reminderTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringType by remember { mutableStateOf(RecurringType.MONTHLY) }
    var whatsappNumber by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var selectedIcon by remember { mutableStateOf(PaymentIconType.GENERIC) }
    
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
                text = "Nuevo Recordatorio",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(
                onClick = {
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
                            iconType = selectedIcon
                        )
                    )
                    onNavigateBack()
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
                    label = { Text("Título del recordatorio") },
                    modifier = Modifier.fillMaxWidth()
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
                        onValueChange = { amount = it },
                        label = { Text("Monto") },
                        modifier = Modifier.weight(1f)
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
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
