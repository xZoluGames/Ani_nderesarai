// app/src/main/java/com/py/ani_nderesarai/ui/viewmodel/HomeViewModel.kt
package com.py.ani_nderesarai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
import com.py.ani_nderesarai.utils.NotificationManager
import com.py.ani_nderesarai.utils.WhatsAppManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PaymentReminderRepository,
    private val whatsAppManager: WhatsAppManager,
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    val activeReminders = repository.getAllActiveReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadReminders()
        checkUpcomingReminders()
    }
    
    private fun loadReminders() {
        viewModelScope.launch {
            repository.getAllActiveReminders().collect { reminders ->
                val today = LocalDate.now()
                val upcomingReminders = reminders.filter { 
                    !it.dueDate.isBefore(today) 
                }.sortedBy { it.dueDate }
                
                val overdueReminders = reminders.filter {
                    it.dueDate.isBefore(today)
                }.sortedBy { it.dueDate }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        upcomingReminders = upcomingReminders,
                        overdueReminders = overdueReminders,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun checkUpcomingReminders() {
        viewModelScope.launch {
            val tomorrow = LocalDate.now().plusDays(1)
            val upcomingReminders = repository.getUpcomingReminders(tomorrow)
            
            upcomingReminders.forEach { reminder ->
                notificationManager.scheduleNotification(reminder)
            }
        }
    }
    
    fun markAsPaid(reminder: PaymentReminder) {
        viewModelScope.launch {
            repository.markAsPaid(reminder.id)
            if (reminder.isRecurring) {
                repository.scheduleNextRecurringReminder(reminder)
            }
            _uiState.update { it.copy(message = "Marcado como pagado") }
        }
    }
    
    fun deleteReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            notificationManager.cancelNotification(reminder.id.toInt())
            _uiState.update { it.copy(message = "Recordatorio eliminado") }
        }
    }
    
    fun sendWhatsAppReminder(context: android.content.Context, reminder: PaymentReminder) {
        val success = whatsAppManager.sendReminder(context, reminder)
        _uiState.update { 
            it.copy(message = if (success) "Enviado por WhatsApp" else "Error al enviar")
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class HomeUiState(
    val upcomingReminders: List<PaymentReminder> = emptyList(),
    val overdueReminders: List<PaymentReminder> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)