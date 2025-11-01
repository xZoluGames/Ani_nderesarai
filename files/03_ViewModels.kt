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

// app/src/main/java/com/py/ani_nderesarai/ui/viewmodel/AddEditReminderViewModel.kt
package com.py.ani_nderesarai.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.py.ani_nderesarai.data.model.*
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
import com.py.ani_nderesarai.utils.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PaymentReminderRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    private val reminderId: Long = savedStateHandle.get<String>("reminderId")?.toLongOrNull() ?: 0L
    
    private val _uiState = MutableStateFlow(AddEditReminderUiState())
    val uiState: StateFlow<AddEditReminderUiState> = _uiState.asStateFlow()
    
    init {
        if (reminderId != 0L) {
            loadReminder()
        }
    }
    
    private fun loadReminder() {
        viewModelScope.launch {
            repository.getReminderById(reminderId)?.let { reminder ->
                _uiState.value = AddEditReminderUiState(
                    reminder = reminder,
                    isEditMode = true
                )
            }
        }
    }
    
    fun saveReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            try {
                val id = if (reminderId != 0L) {
                    repository.updateReminder(reminder.copy(id = reminderId))
                    reminderId
                } else {
                    repository.insertReminder(reminder)
                }
                
                // Programar notificación
                val savedReminder = if (reminderId != 0L) {
                    reminder.copy(id = reminderId)
                } else {
                    reminder.copy(id = id)
                }
                
                notificationManager.scheduleNotification(savedReminder)
                
                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    message = "Recordatorio guardado exitosamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al guardar: ${e.message}"
                )
            }
        }
    }
}

data class AddEditReminderUiState(
    val reminder: PaymentReminder? = null,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
