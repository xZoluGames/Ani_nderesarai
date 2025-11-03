package com.py.ani_nderesarai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.ui.components.getCategoryIcon
import com.py.ani_nderesarai.ui.components.getCategoryName
import com.py.ani_nderesarai.ui.components.getPriorityColor
import com.py.ani_nderesarai.ui.viewmodel.HomeViewModel
import com.py.ani_nderesarai.utils.WhatsAppBotManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddReminder: () -> Unit,
    onNavigateToEditReminder: (Long) -> Unit,
    onNavigateToBotConfig: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val reminders by viewModel.activeReminders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showOverdueSection by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Estado del bot
    val botManager = remember { WhatsAppBotManager(context) }
    val botStatus = remember { botManager.getBotStatus() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ani Nderesarai",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Tus recordatorios de pago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bot WhatsApp") },
                                onClick = {
                                    onNavigateToBotConfig()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (botStatus.isEnabled)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (botStatus.isEnabled) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text("ON", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Estadísticas") },
                                onClick = {
                                    // TODO: Navegar a estadísticas
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.BarChart, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    // TODO: Navegar a configuración
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Acerca de") },
                                onClick = {
                                    // TODO: Mostrar acerca de
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddReminder,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar recordatorio")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (reminders.isEmpty()) {
                EmptyState(
                    onAddClick = onNavigateToAddReminder,
                    onConfigureBotClick = onNavigateToBotConfig
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card del Bot (si tiene recordatorios)
                    if (reminders.isNotEmpty()) {
                        item {
                            BotStatusCard(
                                isEnabled = botStatus.isEnabled,
                                nextExecution = botStatus.nextExecution,
                                onConfigureClick = onNavigateToBotConfig
                            )
                        }
                    }

                    // Sección de vencidos
                    if (uiState.overdueReminders.isNotEmpty()) {
                        item {
                            OverdueSection(
                                overdueCount = uiState.overdueReminders.size,
                                isExpanded = showOverdueSection,
                                onToggle = { showOverdueSection = !showOverdueSection }
                            )
                        }

                        if (showOverdueSection) {
                            items(uiState.overdueReminders) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    isOverdue = true,
                                    onEdit = { onNavigateToEditReminder(reminder.id) },
                                    onMarkAsPaid = { viewModel.markAsPaid(reminder) },
                                    onDelete = { viewModel.deleteReminder(reminder) },
                                    onSendWhatsApp = {
                                        viewModel.sendWhatsAppReminder(context, reminder)
                                    }
                                )
                            }
                        }
                    }

                    // Título de próximos vencimientos
                    if (uiState.upcomingReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Próximos Vencimientos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(uiState.upcomingReminders) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                isOverdue = false,
                                onEdit = { onNavigateToEditReminder(reminder.id) },
                                onMarkAsPaid = { viewModel.markAsPaid(reminder) },
                                onDelete = { viewModel.deleteReminder(reminder) },
                                onSendWhatsApp = {
                                    viewModel.sendWhatsAppReminder(context, reminder)
                                }
                            )
                        }
                    }
                }
            }

            // Snackbar para mensajes
            uiState.message?.let { message ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
fun BotStatusCard(
    isEnabled: Boolean,
    nextExecution: LocalDateTime?,
    onConfigureClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigureClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEnabled) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    }
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = if (isEnabled)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bot WhatsApp",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (isEnabled) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("ACTIVO", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = if (isEnabled) {
                            nextExecution?.let {
                                "Próximo envío: ${formatNextExecution(it)}"
                            } ?: "Configurando..."
                        } else {
                            "Toca para activar notificaciones automáticas"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: PaymentReminder,
    isOverdue: Boolean,
    onEdit: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onDelete: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    val daysUntilDue = ChronoUnit.DAYS.between(today, reminder.dueDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Indicador de prioridad
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getPriorityColor(reminder.priority))
                    )

                    // Icono de categoría
                    Icon(
                        imageVector = getCategoryIcon(reminder.category),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (reminder.description.isNotBlank()) {
                            Text(
                                text = reminder.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Fecha de vencimiento
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = when {
                                            isOverdue -> "Vencido hace ${-daysUntilDue} días"
                                            daysUntilDue == 0L -> "Vence hoy"
                                            daysUntilDue == 1L -> "Vence mañana"
                                            else -> "Vence en $daysUntilDue días"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = when {
                                        isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        daysUntilDue <= 3 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    }
                                )
                            )

                            // Monto si existe
                            reminder.amount?.let { amount ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = formatAmount(amount, reminder.currency),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        // Indicadores adicionales
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (reminder.isRecurring) {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = "Recurrente",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (reminder.whatsappNumber.isNotBlank()) {
                                Icon(
                                    Icons.Default.Whatsapp,
                                    contentDescription = "WhatsApp configurado",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Menú de opciones
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Marcar como pagado") },
                            onClick = {
                                onMarkAsPaid()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }
                        )

                        if (reminder.whatsappNumber.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("Enviar por WhatsApp") },
                                onClick = {
                                    onSendWhatsApp()
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                onEdit()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
                                expanded = false
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
                }
            }
        }
    }
}

@Composable
fun OverdueSection(
    overdueCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "$overdueCount pagos vencidos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
fun EmptyState(
    onAddClick: () -> Unit,
    onConfigureBotClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay recordatorios",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Agrega tu primer recordatorio de pago para empezar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar recordatorio")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onConfigureBotClick) {
            Icon(Icons.Default.SmartToy, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configurar Bot")
        }
    }
}

fun formatAmount(amount: Double, currency: String): String {
    return when (currency) {
        "PYG" -> "₲ ${String.format("%,.0f", amount)}"
        "USD" -> "$ ${String.format("%.2f", amount)}"
        "EUR" -> "€ ${String.format("%.2f", amount)}"
        else -> "$currency ${String.format("%.2f", amount)}"
    }
}

private fun formatNextExecution(nextExecution: LocalDateTime): String {
    val now = LocalDateTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    return when {
        nextExecution.toLocalDate() == now.toLocalDate() -> {
            "Hoy ${nextExecution.format(timeFormatter)}"
        }
        nextExecution.toLocalDate() == now.toLocalDate().plusDays(1) -> {
            "Mañana ${nextExecution.format(timeFormatter)}"
        }
        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
            nextExecution.format(dateFormatter)
        }
    }
}