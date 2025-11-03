package com.py.ani_nderesarai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.py.ani_nderesarai.ui.viewmodel.BotConfigViewModel
import com.py.ani_nderesarai.utils.WhatsAppBotManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: BotConfigViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var phoneNumber by remember { mutableStateOf(uiState.phoneNumber) }
    var selectedHour by remember { mutableStateOf(uiState.sendHour) }
    var selectedMinute by remember { mutableStateOf(uiState.sendMinute) }
    var daysAhead by remember { mutableStateOf(uiState.daysAhead.toString()) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Actualizar valores cuando cambie el estado
    LaunchedEffect(uiState) {
        phoneNumber = uiState.phoneNumber
        selectedHour = uiState.sendHour
        selectedMinute = uiState.sendMinute
        daysAhead = uiState.daysAhead.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bot de WhatsApp") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón de prueba
                    TextButton(
                        onClick = {
                            viewModel.testBot(phoneNumber)
                        }
                    ) {
                        Text("PROBAR", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de Estado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (uiState.isEnabled) "Bot Activado" else "Bot Desactivado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isEnabled && uiState.nextExecution != null) {
                            Text(
                                text = "Próximo envío: ${formatNextExecution(uiState.nextExecution!!)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.enableBot(
                                    phoneNumber = phoneNumber,
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    daysAhead = daysAhead.toIntOrNull() ?: 3
                                )
                            } else {
                                viewModel.disableBot()
                            }
                        }
                    )
                }
            }

            // Card de Información
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Column {
                        Text(
                            text = "¿Cómo funciona?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "El bot enviará automáticamente un resumen de tus pagos próximos por WhatsApp todos los días a la hora configurada.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Configuración
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Número de WhatsApp
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Tu número de WhatsApp") },
                        placeholder = { Text("Ej: 595981234567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Sin el símbolo +, incluye código de país")
                        }
                    )

                    // Hora de envío
                    OutlinedTextField(
                        value = String.format("%02d:%02d", selectedHour, selectedMinute),
                        onValueChange = { },
                        label = { Text("Hora de envío diario") },
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Cambiar hora")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Días de anticipación
                    OutlinedTextField(
                        value = daysAhead,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                daysAhead = it
                            }
                        },
                        label = { Text("Días de anticipación") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Incluir recordatorios de los próximos X días")
                        }
                    )
                }
            }

            // Opciones adicionales
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Opciones Adicionales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Incluir montos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Incluir montos en el resumen")
                        Checkbox(
                            checked = true,
                            onCheckedChange = { }
                        )
                    }

                    // Agrupar por categoría
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Agrupar por categoría")
                        Checkbox(
                            checked = false,
                            onCheckedChange = { }
                        )
                    }

                    // Solo urgentes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solo notificar pagos urgentes")
                        Checkbox(
                            checked = false,
                            onCheckedChange = { }
                        )
                    }
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.testBot(phoneNumber) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Prueba")
                }

                Button(
                    onClick = {
                        viewModel.saveConfiguration(
                            phoneNumber = phoneNumber,
                            hour = selectedHour,
                            minute = selectedMinute,
                            daysAhead = daysAhead.toIntOrNull() ?: 3
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = phoneNumber.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar")
                }
            }

            // Mensaje de estado
            uiState.message?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Seleccionar hora de envío") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("Aceptar")
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

private fun formatNextExecution(nextExecution: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    return when {
        nextExecution.toLocalDate() == now.toLocalDate() -> {
            "Hoy a las ${nextExecution.format(timeFormatter)}"
        }
        nextExecution.toLocalDate() == now.toLocalDate().plusDays(1) -> {
            "Mañana a las ${nextExecution.format(timeFormatter)}"
        }
        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
            "${nextExecution.format(dateFormatter)} a las ${nextExecution.format(timeFormatter)}"
        }
    }
}