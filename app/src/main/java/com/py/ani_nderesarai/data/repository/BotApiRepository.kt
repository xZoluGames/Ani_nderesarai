package com.py.ani_nderesarai.data.repository

import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.network.BotApiService
import com.py.ani_nderesarai.network.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

@Singleton
class BotApiRepository @Inject constructor(
    private val api: BotApiService
) {

    // ============================================
    // HEALTH & STATUS
    // ============================================

    suspend fun checkHealth(): ApiResult<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getHealth()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al verificar salud del servidor")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getBotStatus(): ApiResult<BotStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBotStatus()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error al obtener estado del bot")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexión")
        }
    }

    // ============================================
    // VERIFICACIÓN
    // ============================================

    suspend fun requestVerification(phoneNumber: String): ApiResult<VerificationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = VerificationRequest(phoneNumber)
                val response = api.requestVerification(request)
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Error(errorBody ?: "Error al solicitar verificación")
                }
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Error de conexión")
            }
        }

    suspend fun confirmVerification(
        phoneNumber: String,
        code: String
    ): ApiResult<ConfirmVerificationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ConfirmVerificationRequest(phoneNumber, code)
            val response = api.confirmVerification(request)
            
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                ApiResult.Error(errorBody ?: "Código inválido o expirado")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getVerificationStatus(phoneNumber: String): ApiResult<VerificationStatusResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getVerificationStatus(phoneNumber)
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    ApiResult.Error("Error al verificar estado")
                }
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Error de conexión")
            }
        }

    // ============================================
    // MENSAJES
    // ============================================

    suspend fun sendMessage(
        phoneNumber: String,
        message: String,
        scheduledAt: String? = null
    ): ApiResult<SendMessageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SendMessageRequest(phoneNumber, message, scheduledAt)
            val response = api.sendMessage(request)
            
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                ApiResult.Error(errorBody ?: "Error al enviar mensaje")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun sendRemindersSummary(
        phoneNumber: String,
        reminders: List<PaymentReminder>
    ): ApiResult<SendSummaryResponse> = withContext(Dispatchers.IO) {
        try {
            // Convertir PaymentReminder a ReminderForSummary
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val today = java.time.LocalDate.now()
            
            val remindersList = reminders.map { reminder ->
                ReminderForSummary(
                    title = reminder.title,
                    description = reminder.description.ifBlank { null },
                    dueDate = reminder.dueDate.format(dateFormatter),
                    amount = reminder.amount,
                    currency = reminder.currency,
                    daysUntilDue = ChronoUnit.DAYS.between(today, reminder.dueDate)
                )
            }
            
            val request = SendSummaryRequest(phoneNumber, remindersList)
            val response = api.sendSummary(request)
            
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                ApiResult.Error(errorBody ?: "Error al enviar resumen")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getMessageHistory(phoneNumber: String): ApiResult<MessageHistoryResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMessageHistory(phoneNumber)
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    ApiResult.Error("Error al obtener historial")
                }
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Error de conexión")
            }
        }

    suspend fun getScheduledMessages(phoneNumber: String): ApiResult<ScheduledMessagesResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getScheduledMessages(phoneNumber)
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    ApiResult.Error("Error al obtener mensajes programados")
                }
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Error de conexión")
            }
        }
}
