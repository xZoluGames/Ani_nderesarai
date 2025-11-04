package com.py.ani_nderesarai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.data.repository.BotApiRepository
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
import com.py.ani_nderesarai.data.repository.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class NewBotConfigViewModel @Inject constructor(
    application: Application,
    private val botApiRepository: BotApiRepository,
    private val reminderRepository: PaymentReminderRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NewBotConfigUiState())
    val uiState: StateFlow<NewBotConfigUiState> = _uiState.asStateFlow()

    private val _verificationStep = MutableStateFlow<VerificationStep>(VerificationStep.Initial)
    val verificationStep: StateFlow<VerificationStep> = _verificationStep.asStateFlow()

    init {
        checkBotStatus()
    }

    // ============================================
    // VERIFICACIÓN DE ESTADO DEL BOT
    // ============================================

    private fun checkBotStatus() {
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
                        message = "⚠️ No se pudo conectar con el servidor: ${result.message}",
                        isCheckingBot = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isCheckingBot = false)
                }
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
                        message = "❌ ${result.message}",
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
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
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
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

    /**
     * ✅ ACTUALIZADO: Ahora envía resumen de recordatorios al activar
     */
    fun enableBot(hour: Int, minute: Int, daysAhead: Int) {
        if (!_uiState.value.isVerified) {
            _uiState.value = _uiState.value.copy(
                message = "Por favor verifica tu número primero"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 1. Guardar configuración
                saveBotConfiguration(hour, minute, daysAhead)

                // 2. Obtener recordatorios activos próximos
                val reminders = reminderRepository.getRemindersForBot(daysAhead)

                // 3. Enviar resumen inicial si hay recordatorios
                if (reminders.isNotEmpty()) {
                    val summary = buildRemindersSummary(reminders, hour, minute, daysAhead)

                    when (val result = botApiRepository.sendMessage(
                        _uiState.value.phoneNumber,
                        summary
                    )) {
                        is ApiResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isBotEnabled = true,
                                sendHour = hour,
                                sendMinute = minute,
                                daysAhead = daysAhead,
                                message = "✅ Bot activado. Se envió resumen de ${reminders.size} recordatorio(s)",
                                isLoading = false
                            )
                        }
                        is ApiResult.Error -> {
                            // Activar bot pero informar del error en resumen
                            saveBotConfiguration(hour, minute, daysAhead)
                            _uiState.value = _uiState.value.copy(
                                isBotEnabled = true,
                                sendHour = hour,
                                sendMinute = minute,
                                daysAhead = daysAhead,
                                message = "⚠️ Bot activado pero no se pudo enviar resumen: ${result.message}",
                                isLoading = false
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                } else {
                    // No hay recordatorios, solo activar
                    _uiState.value = _uiState.value.copy(
                        isBotEnabled = true,
                        sendHour = hour,
                        sendMinute = minute,
                        daysAhead = daysAhead,
                        message = "✅ Bot activado (sin recordatorios próximos)",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al activar el bot: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Construye un resumen de recordatorios para enviar por WhatsApp
     */
    private fun buildRemindersSummary(
        reminders: List<PaymentReminder>,
        hour: Int,
        minute: Int,
        daysAhead: Int
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return buildString {
            appendLine("🤖 *Bot de Recordatorios Activado*")
            appendLine()
            appendLine("✅ Configuración guardada:")
            appendLine("⏰ Envío diario: ${String.format("%02d:%02d", hour, minute)}")
            appendLine("📅 Anticipación: $daysAhead día(s)")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📋 *Tienes ${reminders.size} pago(s) próximo(s):*")
            appendLine()

            reminders.forEachIndexed { index, reminder ->
                val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), reminder.dueDate)

                appendLine("*${index + 1}. ${reminder.title}*")
                appendLine("📅 Vence: ${reminder.dueDate.format(dateFormatter)}")

                when {
                    daysUntil == 0L -> appendLine("⚠️ ¡Vence HOY!")
                    daysUntil == 1L -> appendLine("⏰ Vence MAÑANA")
                    daysUntil < 0 -> appendLine("🔴 ¡Vencido hace ${-daysUntil} día(s)!")
                    else -> appendLine("⏰ En $daysUntil día(s)")
                }

                reminder.amount?.let {
                    appendLine("💰 ${formatCurrency(it, reminder.currency)}")
                }

                // Info de cuotas
                if (reminder.isInstallments) {
                    appendLine("📊 Cuota ${reminder.currentInstallment}/${reminder.totalInstallments}")
                }

                // Prioridad urgente
                if (reminder.priority == com.py.ani_nderesarai.data.model.Priority.URGENT) {
                    appendLine("🔴 URGENTE")
                }

                appendLine()
            }

            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("💡 *Recordatorios automáticos*")
            appendLine("Recibirás un resumen cada día a las ${String.format("%02d:%02d", hour, minute)}")
            appendLine()
            appendLine("_Enviado por Ani Nderesarai_ 🤖")
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
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
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

// ============================================
// UI STATE
// ============================================

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

    val isLoading: Boolean = false,
    val message: String? = null
)

sealed class VerificationStep {
    object Initial : VerificationStep()
    object RequestingCode : VerificationStep()
    object CodeSent : VerificationStep()
    object Verifying : VerificationStep()
    object Verified : VerificationStep()
    data class Error(val message: String) : VerificationStep()
}