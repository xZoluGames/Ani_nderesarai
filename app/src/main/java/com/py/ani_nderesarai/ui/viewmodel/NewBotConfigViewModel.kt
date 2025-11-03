package com.py.ani_nderesarai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.py.ani_nderesarai.data.repository.BotApiRepository
import com.py.ani_nderesarai.data.repository.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VerificationStep {
    object Initial : VerificationStep()
    object RequestingCode : VerificationStep()
    object CodeSent : VerificationStep()
    object Verifying : VerificationStep()
    object Verified : VerificationStep()
    data class Error(val message: String) : VerificationStep()
}

@HiltViewModel
class NewBotConfigViewModel @Inject constructor(
    application: Application,
    private val botApiRepository: BotApiRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NewBotConfigUiState())
    val uiState: StateFlow<NewBotConfigUiState> = _uiState.asStateFlow()

    private val _verificationStep = MutableStateFlow<VerificationStep>(VerificationStep.Initial)
    val verificationStep: StateFlow<VerificationStep> = _verificationStep.asStateFlow()

    init {
        checkBotConnection()
    }

    // ============================================
    // VERIFICACIÓN DEL BOT
    // ============================================

    private fun checkBotConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingBot = true)
            
            when (val result = botApiRepository.getBotStatus()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isBotConnected = result.data.connected,
                        botUser = result.data.user,
                        botPhone = result.data.phone,
                        isCheckingBot = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isBotConnected = false,
                        message = "Error: ${result.message}",
                        isCheckingBot = false
                    )
                }
                else -> {}
            }
        }
    }

    // ============================================
    // FLUJO DE VERIFICACIÓN
    // ============================================

    fun requestVerificationCode(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor ingresa tu número de WhatsApp"
            )
            return
        }

        viewModelScope.launch {
            _verificationStep.value = VerificationStep.RequestingCode
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = botApiRepository.requestVerification(phoneNumber)) {
                is ApiResult.Success -> {
                    if (result.data.success == true) {
                        _verificationStep.value = VerificationStep.CodeSent
                        _uiState.value = _uiState.value.copy(
                            message = "✅ Código enviado por WhatsApp",
                            isLoading = false,
                            phoneNumber = phoneNumber
                        )
                    } else {
                        _verificationStep.value = VerificationStep.Error(
                            result.data.message
                        )
                        _uiState.value = _uiState.value.copy(
                            message = result.data.message,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _verificationStep.value = VerificationStep.Error(result.message)
                    _uiState.value = _uiState.value.copy(
                        message = "Error: ${result.message}",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    fun confirmVerificationCode(code: String) {
        if (code.isBlank() || code.length != 6) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor ingresa el código de 6 dígitos"
            )
            return
        }

        val phoneNumber = _uiState.value.phoneNumber
        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Error: número no encontrado"
            )
            return
        }

        viewModelScope.launch {
            _verificationStep.value = VerificationStep.Verifying
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = botApiRepository.confirmVerification(phoneNumber, code)) {
                is ApiResult.Success -> {
                    if (result.data.success == true) {
                        _verificationStep.value = VerificationStep.Verified
                        _uiState.value = _uiState.value.copy(
                            isVerified = true,
                            message = "🎉 ¡Número verificado exitosamente!",
                            isLoading = false
                        )
                        
                        // Guardar en preferencias
                        saveVerifiedPhone(phoneNumber)
                    } else {
                        _verificationStep.value = VerificationStep.Error(
                            result.data.message
                        )
                        _uiState.value = _uiState.value.copy(
                            message = result.data.message,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _verificationStep.value = VerificationStep.Error(result.message)
                    _uiState.value = _uiState.value.copy(
                        message = "❌ ${result.message}",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    fun checkVerificationStatus(phoneNumber: String) {
        viewModelScope.launch {
            when (val result = botApiRepository.getVerificationStatus(phoneNumber)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isVerified = result.data.verified,
                        phoneNumber = if (result.data.verified) phoneNumber else ""
                    )
                    
                    if (result.data.verified) {
                        _verificationStep.value = VerificationStep.Verified
                        saveVerifiedPhone(phoneNumber)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        message = "No se pudo verificar el estado"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetVerification() {
        _verificationStep.value = VerificationStep.Initial
        _uiState.value = _uiState.value.copy(
            isVerified = false,
            phoneNumber = "",
            message = null
        )
    }

    // ============================================
    // GESTIÓN DE BOT AUTOMÁTICO
    // ============================================

    fun enableBot(hour: Int, minute: Int, daysAhead: Int) {
        if (!_uiState.value.isVerified) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor verifica tu número primero"
            )
            return
        }

        viewModelScope.launch {
            try {
                // Guardar configuración
                _uiState.value = _uiState.value.copy(
                    isBotEnabled = true,
                    sendHour = hour,
                    sendMinute = minute,
                    daysAhead = daysAhead,
                    message = "✅ Bot activado exitosamente"
                )
                
                saveBotConfiguration(hour, minute, daysAhead)
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
                _uiState.value = _uiState.value.copy(
                    isBotEnabled = false,
                    message = "Bot desactivado"
                )
                
                clearBotConfiguration()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al desactivar el bot: ${e.message}"
                )
            }
        }
    }

    fun testBotConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = botApiRepository.sendMessage(
                _uiState.value.phoneNumber,
                "✅ Prueba de conexión exitosa.\n\nTu bot de Ani Nderesarai está funcionando correctamente."
            )) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        message = "✅ Mensaje de prueba enviado. Revisa WhatsApp.",
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        message = "❌ Error: ${result.message}",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    // ============================================
    // PERSISTENCIA
    // ============================================

    private fun saveVerifiedPhone(phone: String) {
        val prefs = getApplication<Application>().getSharedPreferences(
            "bot_preferences",
            android.content.Context.MODE_PRIVATE
        )
        prefs.edit()
            .putString("verified_phone", phone)
            .putBoolean("is_verified", true)
            .apply()
    }

    private fun saveBotConfiguration(hour: Int, minute: Int, daysAhead: Int) {
        val prefs = getApplication<Application>().getSharedPreferences(
            "bot_preferences",
            android.content.Context.MODE_PRIVATE
        )
        prefs.edit()
            .putBoolean("bot_enabled", true)
            .putInt("send_hour", hour)
            .putInt("send_minute", minute)
            .putInt("days_ahead", daysAhead)
            .apply()
    }

    private fun clearBotConfiguration() {
        val prefs = getApplication<Application>().getSharedPreferences(
            "bot_preferences",
            android.content.Context.MODE_PRIVATE
        )
        prefs.edit()
            .putBoolean("bot_enabled", false)
            .apply()
    }

    fun loadSavedConfiguration() {
        val prefs = getApplication<Application>().getSharedPreferences(
            "bot_preferences",
            android.content.Context.MODE_PRIVATE
        )
        
        val isVerified = prefs.getBoolean("is_verified", false)
        val verifiedPhone = prefs.getString("verified_phone", "") ?: ""
        val isBotEnabled = prefs.getBoolean("bot_enabled", false)
        val hour = prefs.getInt("send_hour", 9)
        val minute = prefs.getInt("send_minute", 0)
        val daysAhead = prefs.getInt("days_ahead", 3)
        
        _uiState.value = _uiState.value.copy(
            isVerified = isVerified,
            phoneNumber = verifiedPhone,
            isBotEnabled = isBotEnabled,
            sendHour = hour,
            sendMinute = minute,
            daysAhead = daysAhead
        )
        
        if (isVerified && verifiedPhone.isNotBlank()) {
            _verificationStep.value = VerificationStep.Verified
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class NewBotConfigUiState(
    val isBotConnected: Boolean = false,
    val botUser: String? = null,
    val botPhone: String? = null,
    val isCheckingBot: Boolean = false,
    val isVerified: Boolean = false,
    val phoneNumber: String = "",
    val isBotEnabled: Boolean = false,
    val sendHour: Int = 9,
    val sendMinute: Int = 0,
    val daysAhead: Int = 3,
    val message: String? = null,
    val isLoading: Boolean = false
)
