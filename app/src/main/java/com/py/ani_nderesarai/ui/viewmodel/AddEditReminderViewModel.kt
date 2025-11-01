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
