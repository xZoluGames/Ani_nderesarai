package com.py.ani_nderesarai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.py.ani_nderesarai.utils.WhatsAppBotManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BotConfigViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val botManager = WhatsAppBotManager(application)

    private val _uiState = MutableStateFlow(BotConfigUiState())
    val uiState: StateFlow<BotConfigUiState> = _uiState.asStateFlow()

    init {
        loadCurrentConfiguration()
    }

    private fun loadCurrentConfiguration() {
        val status = botManager.getBotStatus()
        _uiState.value = BotConfigUiState(
            isEnabled = status.isEnabled,
            phoneNumber = status.phoneNumber,
            sendHour = status.sendTime.first,
            sendMinute = status.sendTime.second,
            daysAhead = status.daysAhead,
            nextExecution = status.nextExecution
        )
    }

    fun enableBot(
        phoneNumber: String,
        hour: Int,
        minute: Int,
        daysAhead: Int
    ) {
        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor ingresa tu número de WhatsApp"
            )
            return
        }

        viewModelScope.launch {
            try {
                botManager.scheduleBot(
                    phoneNumber = phoneNumber,
                    hour = hour,
                    minute = minute,
                    daysAhead = daysAhead
                )

                _uiState.value = _uiState.value.copy(
                    isEnabled = true,
                    phoneNumber = phoneNumber,
                    sendHour = hour,
                    sendMinute = minute,
                    daysAhead = daysAhead,
                    message = "Bot activado exitosamente",
                    nextExecution = calculateNextExecution(hour, minute)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al activar el bot: ${e.message}"
                )
            }
        }
    }

    fun disableBot() {
        viewModelScope.launch {
            try {
                botManager.stopBot()
                _uiState.value = _uiState.value.copy(
                    isEnabled = false,
                    message = "Bot desactivado",
                    nextExecution = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al desactivar el bot: ${e.message}"
                )
            }
        }
    }

    fun testBot(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor ingresa tu número de WhatsApp"
            )
            return
        }

        viewModelScope.launch {
            try {
                botManager.runBotNow(phoneNumber)
                _uiState.value = _uiState.value.copy(
                    message = "Enviando mensaje de prueba... Revisa WhatsApp"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al enviar prueba: ${e.message}"
                )
            }
        }
    }

    fun saveConfiguration(
        phoneNumber: String,
        hour: Int,
        minute: Int,
        daysAhead: Int
    ) {
        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor ingresa tu número de WhatsApp"
            )
            return
        }

        viewModelScope.launch {
            try {
                // Si el bot está activado, reprogramarlo con nueva configuración
                if (_uiState.value.isEnabled) {
                    botManager.scheduleBot(
                        phoneNumber = phoneNumber,
                        hour = hour,
                        minute = minute,
                        daysAhead = daysAhead
                    )
                }

                _uiState.value = _uiState.value.copy(
                    phoneNumber = phoneNumber,
                    sendHour = hour,
                    sendMinute = minute,
                    daysAhead = daysAhead,
                    message = "Configuración guardada",
                    nextExecution = if (_uiState.value.isEnabled) {
                        calculateNextExecution(hour, minute)
                    } else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun calculateNextExecution(hour: Int, minute: Int): LocalDateTime {
        val now = LocalDateTime.now()
        val targetTime = java.time.LocalTime.of(hour, minute)

        return if (now.toLocalTime().isBefore(targetTime)) {
            now.toLocalDate().atTime(targetTime)
        } else {
            now.toLocalDate().plusDays(1).atTime(targetTime)
        }
    }
}

data class BotConfigUiState(
    val isEnabled: Boolean = false,
    val phoneNumber: String = "",
    val sendHour: Int = 9,
    val sendMinute: Int = 0,
    val daysAhead: Int = 3,
    val nextExecution: LocalDateTime? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)