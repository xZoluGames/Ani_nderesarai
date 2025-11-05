package com.py.ani_nderesarai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.ui.components.FilterDialog
import com.py.ani_nderesarai.ui.components.ReminderCard
import com.py.ani_nderesarai.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddReminder: () -> Unit,
    onNavigateToEditReminder: (Long) -> Unit,
    onNavigateToBotConfig: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val paidReminders by viewModel.paidReminders.collectAsState()
    val cancelledReminders by viewModel.cancelledReminders.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<PaymentReminder?>(null) }

    // Limpiar mensaje después de 3 segundos
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ani Nderesarai",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!uiState.isLoading && uiState.activeCount > 0) {
                            Text(
                                text = "${uiState.activeCount} activos • ${uiState.overdueCount} vencidos",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.overdueCount > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Botón de búsqueda/filtros
                    IconButton(onClick = { viewModel.toggleFilterDialog() }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }

                    // Menú
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
                                    Icon(Icons.Default.SmartToy, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Actualizar") },
                                onClick = {
                                    viewModel.refreshReminders()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sistema de Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activos")
                            if (uiState.activeCount > 0) {
                                Badge {
                                    Text("${uiState.activeCount}")
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pagados")
                            if (uiState.paidCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                    Text("${uiState.paidCount}")
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("Cancelados") },
                    icon = { Icon(Icons.Default.Cancel, contentDescription = null) }
                )
            }

            // Contenido de tabs
            when (selectedTab) {
                0 -> ActiveTab(
                    activeReminders = activeReminders,
                    overdueReminders = uiState.overdueReminders,
                    isLoading = uiState.isLoading,
                    onEdit = onNavigateToEditReminder,
                    onMarkAsPaid = { viewModel.markAsPaid(it) },
                    onCancel = { viewModel.cancelReminder(it) },
                    onDelete = { showDeleteDialog = it },
                    onSendWhatsApp = { viewModel.sendWhatsAppReminder(it) },
                    onAddClick = onNavigateToAddReminder,
                    onConfigureBotClick = onNavigateToBotConfig
                )

                1 -> PaidTab(
                    paidReminders = paidReminders,
                    onEdit = onNavigateToEditReminder,
                    onDelete = { showDeleteDialog = it }
                )

                2 -> CancelledTab(
                    cancelledReminders = cancelledReminders,
                    onReactivate = { viewModel.reactivateReminder(it) },
                    onDelete = { showDeleteDialog = it }
                )
            }

            // Snackbar para mensajes
            uiState.message?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
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

    // Dialog de confirmación de eliminación
    showDeleteDialog?.let { reminder ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar recordatorio") },
            text = {
                Text("¿Estás seguro de eliminar \"${reminder.title}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReminder(reminder)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de filtros
    if (uiState.showFilterDialog) {
        FilterDialog(
            onDismiss = { viewModel.toggleFilterDialog() },
            onApplyFilters = { filters ->
                viewModel.applyFilters(filters)
                viewModel.toggleFilterDialog()
            },
            onClearFilters = {
                viewModel.clearFilters()
                viewModel.toggleFilterDialog()
            }
        )
    }
}

// ============================================
// TAB DE ACTIVOS
// ============================================

@Composable
fun ActiveTab(
    activeReminders: List<PaymentReminder>,
    overdueReminders: List<PaymentReminder>,
    isLoading: Boolean,
    onEdit: (Long) -> Unit,
    onMarkAsPaid: (PaymentReminder) -> Unit,
    onCancel: (PaymentReminder) -> Unit,
    onDelete: (PaymentReminder) -> Unit,
    onSendWhatsApp: (PaymentReminder) -> Unit,
    onAddClick: () -> Unit,
    onConfigureBotClick: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (activeReminders.isEmpty()) {
        EmptyState(
            onAddClick = onAddClick,
            onConfigureBotClick = onConfigureBotClick
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección de vencidos
            if (overdueReminders.isNotEmpty()) {
                item {
                    Text(
                        text = "⚠️ Vencidos (${overdueReminders.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(overdueReminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        isOverdue = true,
                        onEdit = { onEdit(reminder.id) },
                        onMarkAsPaid = { onMarkAsPaid(reminder) },
                        onCancel = { onCancel(reminder) },
                        onDelete = { onDelete(reminder) },
                        onSendWhatsApp = { onSendWhatsApp(reminder) }
                    )
                }
            }

            // Sección de próximos
            val upcoming = activeReminders.filter { it.dueDate >= java.time.LocalDate.now() }
            if (upcoming.isNotEmpty()) {
                item {
                    Text(
                        text = "📅 Próximos (${upcoming.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(upcoming) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        isOverdue = false,
                        onEdit = { onEdit(reminder.id) },
                        onMarkAsPaid = { onMarkAsPaid(reminder) },
                        onCancel = { onCancel(reminder) },
                        onDelete = { onDelete(reminder) },
                        onSendWhatsApp = { onSendWhatsApp(reminder) }
                    )
                }
            }
        }
    }
}

// ============================================
// TAB DE PAGADOS
// ============================================

@Composable
fun PaidTab(
    paidReminders: List<PaymentReminder>,
    onEdit: (Long) -> Unit,
    onDelete: (PaymentReminder) -> Unit
) {
    if (paidReminders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                )
                Text(
                    text = "No hay pagos registrados",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(paidReminders) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    isPaid = true,
                    onEdit = { onEdit(reminder.id) },
                    onDelete = { onDelete(reminder) },
                    onMarkAsPaid = {},  // Ya está pagado
                    onCancel = {},      // No aplica
                    onSendWhatsApp = {} // No aplica
                )
            }
        }
    }
}

// ============================================
// TAB DE CANCELADOS
// ============================================

@Composable
fun CancelledTab(
    cancelledReminders: List<PaymentReminder>,
    onReactivate: (PaymentReminder) -> Unit,
    onDelete: (PaymentReminder) -> Unit
) {
    if (cancelledReminders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "No hay deudas canceladas",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cancelledReminders) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    isCancelled = true,
                    onReactivate = { onReactivate(reminder) },
                    onDelete = { onDelete(reminder) },
                    onEdit = {},
                    onMarkAsPaid = {},
                    onCancel = {},
                    onSendWhatsApp = {}
                )
            }
        }
    }
}

// ============================================
// ESTADO VACÍO
// ============================================

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