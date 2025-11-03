package com.py.ani_nderesarai.utils

import android.content.Context
import androidx.work.*
import com.py.ani_nderesarai.workers.WhatsAppBotWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Manager para controlar el bot de WhatsApp
 */
class WhatsAppBotManager(private val context: Context) {

    companion object {
        private const val WORK_NAME = "whatsapp_daily_bot"
        private const val PREFS_NAME = "bot_config"
        private const val KEY_BOT_ENABLED = "bot_enabled"
        private const val KEY_PHONE_NUMBER = "default_phone"
        private const val KEY_SEND_HOUR = "send_hour"
        private const val KEY_SEND_MINUTE = "send_minute"
        private const val KEY_DAYS_AHEAD = "days_ahead"

        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
        const val DEFAULT_DAYS_AHEAD = 3
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(context)

    /**
     * Programa el bot para ejecutarse diariamente
     */
    fun scheduleBot(
        phoneNumber: String,
        hour: Int = DEFAULT_HOUR,
        minute: Int = DEFAULT_MINUTE,
        daysAhead: Int = DEFAULT_DAYS_AHEAD
    ) {
        // Guardar configuración
        prefs.edit()
            .putBoolean(KEY_BOT_ENABLED, true)
            .putString(KEY_PHONE_NUMBER, phoneNumber)
            .putInt(KEY_SEND_HOUR, hour)
            .putInt(KEY_SEND_MINUTE, minute)
            .putInt(KEY_DAYS_AHEAD, daysAhead)
            .apply()

        // Calcular delay inicial para la hora especificada
        val initialDelay = calculateInitialDelay(hour, minute)

        // Configurar constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No requiere internet obligatorio
            .setRequiresBatteryNotLow(false) // Ejecutar aunque batería baja
            .build()

        // Crear datos de entrada
        val inputData = workDataOf(
            WhatsAppBotWorker.KEY_PHONE_NUMBER to phoneNumber,
            WhatsAppBotWorker.KEY_SEND_HOUR to hour,
            WhatsAppBotWorker.KEY_DAYS_AHEAD to daysAhead
        )

        // Crear trabajo periódico (cada 24 horas)
        val botWork = PeriodicWorkRequestBuilder<WhatsAppBotWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("whatsapp_bot")
            .build()

        // Programar trabajo único
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Actualizar si ya existe
            botWork
        )
    }

    /**
     * Detiene el bot
     */
    fun stopBot() {
        prefs.edit()
            .putBoolean(KEY_BOT_ENABLED, false)
            .apply()

        workManager.cancelUniqueWork(WORK_NAME)
    }

    /**
     * Ejecuta el bot inmediatamente (para pruebas)
     */
    fun runBotNow(phoneNumber: String? = null, daysAhead: Int? = null) {
        val savedPhone = phoneNumber ?: getSavedPhoneNumber()
        val savedDays = daysAhead ?: getSavedDaysAhead()

        val inputData = workDataOf(
            WhatsAppBotWorker.KEY_PHONE_NUMBER to savedPhone,
            WhatsAppBotWorker.KEY_DAYS_AHEAD to savedDays
        )

        val oneTimeWork = OneTimeWorkRequestBuilder<WhatsAppBotWorker>()
            .setInputData(inputData)
            .addTag("whatsapp_bot_test")
            .build()

        workManager.enqueue(oneTimeWork)
    }

    /**
     * Verifica si el bot está activado
     */
    fun isBotEnabled(): Boolean {
        return prefs.getBoolean(KEY_BOT_ENABLED, false)
    }

    /**
     * Obtiene el número de teléfono guardado
     */
    fun getSavedPhoneNumber(): String {
        return prefs.getString(KEY_PHONE_NUMBER, "") ?: ""
    }

    /**
     * Obtiene la hora de envío guardada
     */
    fun getSavedSendTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_SEND_HOUR, DEFAULT_HOUR)
        val minute = prefs.getInt(KEY_SEND_MINUTE, DEFAULT_MINUTE)
        return Pair(hour, minute)
    }

    /**
     * Obtiene los días de anticipación guardados
     */
    fun getSavedDaysAhead(): Int {
        return prefs.getInt(KEY_DAYS_AHEAD, DEFAULT_DAYS_AHEAD)
    }

    /**
     * Calcula el delay inicial para ejecutar a la hora especificada
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(hour, minute)

        // Si la hora objetivo ya pasó hoy, programar para mañana
        val targetDateTime = if (now.toLocalTime().isBefore(targetTime)) {
            now.toLocalDate().atTime(targetTime)
        } else {
            now.toLocalDate().plusDays(1).atTime(targetTime)
        }

        return Duration.between(now, targetDateTime).toMillis()
    }

    /**
     * Obtiene información del estado del bot
     */
    fun getBotStatus(): BotStatus {
        return BotStatus(
            isEnabled = isBotEnabled(),
            phoneNumber = getSavedPhoneNumber(),
            sendTime = getSavedSendTime(),
            daysAhead = getSavedDaysAhead(),
            nextExecution = calculateNextExecution()
        )
    }

    private fun calculateNextExecution(): LocalDateTime? {
        if (!isBotEnabled()) return null

        val (hour, minute) = getSavedSendTime()
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(hour, minute)

        return if (now.toLocalTime().isBefore(targetTime)) {
            now.toLocalDate().atTime(targetTime)
        } else {
            now.toLocalDate().plusDays(1).atTime(targetTime)
        }
    }

    data class BotStatus(
        val isEnabled: Boolean,
        val phoneNumber: String,
        val sendTime: Pair<Int, Int>,
        val daysAhead: Int,
        val nextExecution: LocalDateTime?
    )
}