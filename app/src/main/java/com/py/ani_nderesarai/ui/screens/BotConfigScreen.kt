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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.py.ani_nderesarai.ui.viewmodel.NewBotConfigViewModel
import com.py.ani_nderesarai.ui.viewmodel.VerificationStep
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewBotConfigViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val verificationStep by viewModel.verificationStep.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var daysAhead by remember { mutableStateOf("3") }
    var showTimePicker by remember { mutableStateOf(false) }

    // Cargar configuración guardada
    LaunchedEffect(Unit) {
        viewModel.loadSavedConfiguration()
    }

    // Actualizar valores cuando cambie el estado
    LaunchedEffect(uiState) {
        if (uiState.phoneNumber.isNotBlank()) {
            phoneNumber = uiState.phoneNumber
        }
        selectedHour = uiState.sendHour
        selectedMinute = uiState.sendMinute
        daysAhead = uiState.daysAhead.toString()
    }

    // Mostrar mensajes
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bot de WhatsApp") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            // Card de información
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
                            text = "Bot Automático de WhatsApp",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "El bot enviará automáticamente un resumen de tus pagos próximos por WhatsApp desde nuestro servidor.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Estado de conexión del bot en la VPS
            if (uiState.isCheckingBot) {
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Verificando conexión con el servidor...")
                    }
                }
            } else if (uiState.isBotConnected) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "✅ Servidor conectado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            uiState.botUser?.let {
                                Text(
                                    text = "WhatsApp: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Sección de verificación
            when (verificationStep) {
                is VerificationStep.Initial -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Paso 1: Verificar tu número",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

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
                                },
                                enabled = !uiState.isLoading
                            )

                            Button(
                                onClick = { viewModel.requestVerificationCode(phoneNumber) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = phoneNumber.isNotBlank() && !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Solicitar código de verificación")
                            }
                        }
                    }
                }

                is VerificationStep.RequestingCode -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Solicitando código...")
                        }
                    }
                }

                is VerificationStep.CodeSent -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Paso 2: Ingresar código",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Te enviamos un código de 6 dígitos por WhatsApp a: $phoneNumber",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        verificationCode = it
                                    }
                                },
                                label = { Text("Código de verificación") },
                                placeholder = { Text("123456") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isLoading
                            )

                            Button(
                                onClick = { viewModel.confirmVerificationCode(verificationCode) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = verificationCode.length == 6 && !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Verificar código")
                            }

                            TextButton(
                                onClick = { viewModel.requestVerificationCode(phoneNumber) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reenviar código")
                            }
                        }
                    }
                }

                is VerificationStep.Verifying -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verificando código...")
                        }
                    }
                }

                is VerificationStep.Verified -> {
                    // Usuario verificado - mostrar configuración del bot
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "✅ Número verificado",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "WhatsApp: $phoneNumber",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Configuración del bot
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Configuración del Bot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Hora de envío
                            OutlinedTextField(
                                value = String.format("%02d:%02d", selectedHour, selectedMinute),
                                onValueChange = { },
                                label = { Text("Hora de envío diario") },
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.AccessTime, contentDescription = null)
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
                                placeholder = { Text("3") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = {
                                    Text("Notificar pagos que vencen en los próximos X días")
                                }
                            )

                            // Switch del bot
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (uiState.isBotEnabled) "Bot Activado" else "Bot Desactivado",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (uiState.isBotEnabled)
                                            "Enviando resúmenes automáticamente"
                                        else
                                            "Activa el bot para recibir resúmenes",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Switch(
                                    checked = uiState.isBotEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            viewModel.enableBot(
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

                            // Botón de prueba
                            OutlinedButton(
                                onClick = { viewModel.testBotConnection() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar mensaje de prueba")
                            }
                        }
                    }
                }

                is VerificationStep.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = (verificationStep as VerificationStep.Error).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Button(
                                onClick = { viewModel.resetVerification() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Intentar nuevamente")
                            }
                        }
                    }
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