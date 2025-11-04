package com.py.ani_nderesarai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.py.ani_nderesarai.data.model.*
import com.py.ani_nderesarai.data.repository.BotApiRepository
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
import com.py.ani_nderesarai.data.repository.ApiResult
import com.py.ani_nderesarai.utils.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: PaymentReminderRepository,
    private val botApiRepository: BotApiRepository,
    private val notificationManager: NotificationManager
) : AndroidViewModel(application) {

    // Estados de UI
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Recordatorios por estado
    private val _activeReminders = MutableStateFlow<List<PaymentReminder>>(emptyList())
    val activeReminders: StateFlow<List<PaymentReminder>> = _activeReminders.asStateFlow()

    private val _paidReminders = MutableStateFlow<List<PaymentReminder>>(emptyList())
    val paidReminders: StateFlow<List<PaymentReminder>> = _paidReminders.asStateFlow()

    private val _cancelledReminders = MutableStateFlow<List<PaymentReminder>>(emptyList())
    val cancelledReminders: StateFlow<List<PaymentReminder>> = _cancelledReminders.asStateFlow()

    // Filtros
    private val _currentFilters = MutableStateFlow(ReminderFilters())
    val currentFilters: StateFlow<ReminderFilters> = _currentFilters.asStateFlow()

    // Tab seleccionada
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadAllReminders()
        updateOverdueStatus()
    }

    // ============================================
    // CARGA DE DATOS
    // ============================================

    private fun loadAllReminders() {
        viewModelScope.launch {
            // Activos
            repository.getAllActiveReminders().collect { reminders ->
                val now = LocalDate.now()
                _activeReminders.value = reminders

                // Separar vencidos y próximos
                val overdue = reminders.filter { it.dueDate < now }
                val upcoming = reminders.filter { it.dueDate >= now }

                _uiState.update {
                    it.copy(
                        overdueReminders = overdue,
                        upcomingReminders = upcoming,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            // Pagados
            repository.getPaidReminders().collect { paid ->
                _paidReminders.value = paid
            }
        }

        viewModelScope.launch {
            // Cancelados
            repository.getCancelledReminders().collect { cancelled ->
                _cancelledReminders.value = cancelled
            }
        }

        viewModelScope.launch {
            // Estadísticas
            val stats = repository.getStatistics()
            _uiState.update {
                it.copy(
                    activeCount = stats.activeCount,
                    overdueCount = stats.overdueCount,
                    paidCount = stats.paidCount,
                    totalPendingAmount = stats.totalPendingAmount
                )
            }
        }
    }

    fun refreshReminders() {
        _uiState.update { it.copy(isLoading = true) }
        loadAllReminders()
    }

    private fun updateOverdueStatus() {
        viewModelScope.launch {
            repository.updateOverdueStatus()
        }
    }

    // ============================================
    // GESTIÓN DE ESTADOS
    // ============================================

    /**
     * Marca un recordatorio como pagado
     * Maneja cuotas y recurrencia automáticamente
     */
    fun markAsPaid(reminder: PaymentReminder) {
        viewModelScope.launch {
            try {
                repository.markAsPaid(reminder)

                // Mensaje según tipo
                val message = when {
                    reminder.isInstallments && reminder.currentInstallment < reminder.totalInstallments -> {
                        "✅ Cuota ${reminder.currentInstallment} pagada. Próxima: cuota ${reminder.currentInstallment + 1}"
                    }
                    reminder.isRecurring -> {
                        "✅ Pago marcado. Se ha programado el próximo recordatorio"
                    }
                    else -> {
                        "✅ Marcado como pagado"
                    }
                }

                _uiState.update { it.copy(message = message) }
                refreshReminders()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "❌ Error: ${e.message}") }
            }
        }
    }

    /**
     * Cancela un recordatorio
     */
    fun cancelReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            try {
                repository.cancelReminder(reminder.id)
                notificationManager.cancelNotification(reminder.id.toInt())

                _uiState.update { it.copy(message = "🗑️ Deuda cancelada") }
                refreshReminders()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "❌ Error: ${e.message}") }
            }
        }
    }

    /**
     * Reactiva un recordatorio cancelado
     */
    fun reactivateReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            try {
                repository.reactivateReminder(reminder.id)

                // Reprogramar notificaciones
                val reactivated = repository.getReminderById(reminder.id)
                reactivated?.let {
                    notificationManager.scheduleNotification(it)
                }

                _uiState.update { it.copy(message = "♻️ Recordatorio reactivado") }
                refreshReminders()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "❌ Error: ${e.message}") }
            }
        }
    }

    /**
     * Elimina permanentemente un recordatorio
     */
    fun deleteReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(reminder)
                notificationManager.cancelNotification(reminder.id.toInt())

                _uiState.update { it.copy(message = "🗑️ Recordatorio eliminado") }
                refreshReminders()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "❌ Error: ${e.message}") }
            }
        }
    }

    // ============================================
    // ENVÍO POR WHATSAPP (VÍA API)
    // ============================================

    /**
     * Envía recordatorio individual por WhatsApp usando el bot (API)
     */
    fun sendWhatsAppReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Obtener número verificado
                val verifiedPhone = getVerifiedPhoneNumber()

                if (verifiedPhone.isBlank()) {
                    _uiState.update {
                        it.copy(
                            message = "⚠️ Configure el bot primero para verificar su número",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Construir mensaje
                val message = buildReminderMessage(reminder)

                // Enviar por API
                when (val result = botApiRepository.sendMessage(verifiedPhone, message)) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                message = "✅ Recordatorio enviado por WhatsApp",
                                isLoading = false
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                message = "❌ Error al enviar: ${result.message}",
                                isLoading = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = "❌ Error: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun getVerifiedPhoneNumber(): String {
        val prefs = getApplication<Application>().getSharedPreferences(
            "bot_preferences",
            android.content.Context.MODE_PRIVATE
        )
        return prefs.getString("verified_phone", "") ?: ""
    }

    private fun buildReminderMessage(reminder: PaymentReminder): String {
        val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), reminder.dueDate)
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return buildString {
            appendLine("📋 *Recordatorio de Pago*")
            appendLine()
            appendLine("*${reminder.title}*")

            if (reminder.description.isNotBlank()) {
                appendLine(reminder.description)
                appendLine()
            }

            appendLine("📅 Vence: ${reminder.dueDate.format(dateFormatter)}")

            if (daysUntil >= 0) {
                appendLine("⏰ Faltan $daysUntil día(s)")
            } else {
                appendLine("⚠️ ¡Vencido hace ${-daysUntil} día(s)!")
            }

            reminder.amount?.let {
                appendLine()
                appendLine("💰 Monto: ${formatCurrency(it, reminder.currency)}")
            }

            // Info de cuotas
            if (reminder.isInstallments) {
                appendLine()
                appendLine("📊 Cuota ${reminder.currentInstallment} de ${reminder.totalInstallments}")
            }

            // Prioridad
            if (reminder.priority == Priority.URGENT || reminder.priority == Priority.HIGH) {
                appendLine()
                appendLine("🔴 Prioridad: ${reminder.priority.name}")
            }

            if (reminder.notes.isNotBlank()) {
                appendLine()
                appendLine("📝 Notas: ${reminder.notes}")
            }
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

    // ============================================
    // FILTROS
    // ============================================

    fun applyFilters(filters: ReminderFilters) {
        viewModelScope.launch {
            _currentFilters.value = filters

            repository.getFilteredReminders(filters).collect { filtered ->
                _activeReminders.value = filtered
            }
        }
    }

    fun clearFilters() {
        _currentFilters.value = ReminderFilters()
        loadAllReminders()
    }

    fun searchReminders(query: String) {
        if (query.isBlank()) {
            clearFilters()
            return
        }

        viewModelScope.launch {
            repository.searchReminders(query).collect { results ->
                _activeReminders.value = results
            }
        }
    }

    // ============================================
    // NAVEGACIÓN DE TABS
    // ============================================

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // ============================================
    // UI HELPERS
    // ============================================

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun toggleFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = !it.showFilterDialog) }
    }
}

// ============================================
// UI STATE
// ============================================

data class HomeUiState(
    val upcomingReminders: List<PaymentReminder> = emptyList(),
    val overdueReminders: List<PaymentReminder> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val showFilterDialog: Boolean = false,

    // Estadísticas
    val activeCount: Int = 0,
    val overdueCount: Int = 0,
    val paidCount: Int = 0,
    val totalPendingAmount: Double = 0.0
)