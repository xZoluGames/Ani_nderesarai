package com.py.ani_nderesarai.workers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.py.ani_nderesarai.data.database.AppDatabase
import com.py.ani_nderesarai.data.model.PaymentReminder
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Worker que ejecuta diariamente para enviar un resumen de pagos por WhatsApp
 */
class WhatsAppBotWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_PHONE_NUMBER = "phone_number"
        const val KEY_SEND_HOUR = "send_hour"
        const val KEY_DAYS_AHEAD = "days_ahead"
        const val DEFAULT_DAYS_AHEAD = 3
    }

    override suspend fun doWork(): Result {
        return try {
            val phoneNumber = inputData.getString(KEY_PHONE_NUMBER)
                ?: getDefaultPhoneNumber()
            val daysAhead = inputData.getInt(KEY_DAYS_AHEAD, DEFAULT_DAYS_AHEAD)

            val reminders = getUpcomingReminders(daysAhead)

            if (reminders.isNotEmpty()) {
                sendWhatsAppSummary(reminders, phoneNumber)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun getUpcomingReminders(daysAhead: Int): List<PaymentReminder> {
        val database = AppDatabase.getDatabase(applicationContext)
        val today = LocalDate.now()
        val endDate = today.plusDays(daysAhead.toLong())

        return database.paymentReminderDao()
            .getRemindersByDateRange(today, endDate)
            .first()
            .filter { it.isActive }
            .sortedBy { it.dueDate }
    }

    private fun sendWhatsAppSummary(reminders: List<PaymentReminder>, phoneNumber: String) {
        val message = buildSummaryMessage(reminders)

        // Crear intent para abrir WhatsApp
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setPackage("com.whatsapp") // Intentar con WhatsApp regular primero
        }

        // Si WhatsApp no está instalado, intentar con WhatsApp Business
        if (intent.resolveActivity(applicationContext.packageManager) == null) {
            intent.setPackage("com.whatsapp.w4b")
        }

        // Si ninguno está instalado, abrir en navegador
        if (intent.resolveActivity(applicationContext.packageManager) == null) {
            intent.setPackage(null)
        }

        applicationContext.startActivity(intent)
    }

    private fun buildSummaryMessage(reminders: List<PaymentReminder>): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

        return buildString {
            append("🤖 *Ani Nderesarai - Bot Automático*\n")
            append("📅 *Resumen de Pagos Próximos*\n")
            append("━━━━━━━━━━━━━━━━━━\n\n")

            val today = LocalDate.now()
            val groupedByDate = reminders.groupBy { it.dueDate }

            var totalAmount = 0.0
            var currency = "PYG"

            groupedByDate.forEach { (date, dayReminders) ->
                val daysUntil = ChronoUnit.DAYS.between(today, date)

                // Emoji y título según urgencia
                val (emoji, title) = when (daysUntil) {
                    0L -> "🔴" to "*HOY - URGENTE*"
                    1L -> "🟡" to "*MAÑANA*"
                    2L -> "🟠" to "*PASADO MAÑANA*"
                    else -> "🟢" to "*${date.format(dateFormatter)} (en $daysUntil días)*"
                }

                append("$emoji $title\n")

                dayReminders.forEach { reminder ->
                    append("  • ${reminder.title}")

                    reminder.amount?.let { amount ->
                        append(" → ${formatCurrency(amount, reminder.currency)}")
                        totalAmount += amount
                        currency = reminder.currency
                    }

                    if (reminder.description.isNotBlank()) {
                        append("\n    📝 ${reminder.description}")
                    }

                    append("\n")
                }
                append("\n")
            }

            // Resumen total
            if (totalAmount > 0) {
                append("━━━━━━━━━━━━━━━━━━\n")
                append("💰 *TOTAL: ${formatCurrency(totalAmount, currency)}*\n")
            }

            append("\n")

            // Mensaje motivacional según la situación
            val overdueCount = reminders.count {
                ChronoUnit.DAYS.between(today, it.dueDate) <= 0
            }

            when {
                overdueCount > 0 -> {
                    append("⚠️ *Tienes pagos vencidos o que vencen hoy.*\n")
                    append("_¡Realízalos lo antes posible para evitar recargos!_")
                }
                reminders.any { ChronoUnit.DAYS.between(today, it.dueDate) == 1L } -> {
                    append("⏰ *Recuerda preparar estos pagos para mañana.*")
                }
                else -> {
                    append("✅ *Todo bajo control. ¡Sigue así!*")
                }
            }

            append("\n\n")
            append("_Enviado automáticamente por Ani Nderesarai_")
            append("\n")
            append("_Para desactivar: Configuración → Bot WhatsApp_")
        }
    }

    private fun formatCurrency(amount: Double, currency: String): String {
        return when (currency) {
            "PYG" -> "₲ ${String.format("%,.0f", amount)}"
            "USD" -> "$ ${String.format("%,.2f", amount)}"
            "EUR" -> "€ ${String.format("%,.2f", amount)}"
            "BRL" -> "R$ ${String.format("%,.2f", amount)}"
            "ARS" -> "$ ${String.format("%,.2f", amount)} ARS"
            else -> "${String.format("%,.2f", amount)} $currency"
        }
    }

    private fun getDefaultPhoneNumber(): String {
        // Obtener de SharedPreferences
        val prefs = applicationContext.getSharedPreferences("bot_config", Context.MODE_PRIVATE)
        return prefs.getString("default_phone", "") ?: ""
    }
}