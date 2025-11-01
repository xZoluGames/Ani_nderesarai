// app/src/main/java/com/py/ani_nderesarai/utils/NotificationManager.kt
package com.py.ani_nderesarai.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.py.ani_nderesarai.MainActivity
import com.py.ani_nderesarai.R
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.workers.AlarmReceiver
import java.time.*

class NotificationManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "payment_reminders"
        const val CHANNEL_NAME = "Recordatorios de Pago"
        const val CHANNEL_DESCRIPTION = "Notificaciones para recordatorios de pagos importantes"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleNotification(reminder: PaymentReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Programar notificaciones para los días antes configurados
        reminder.reminderDaysBefore.forEach { daysBefore ->
            val notificationDate = reminder.dueDate.minusDays(daysBefore.toLong())
            val notificationDateTime = LocalDateTime.of(notificationDate, reminder.reminderTime)
            
            // Solo programar si la fecha es futura
            if (notificationDateTime.isAfter(LocalDateTime.now())) {
                val triggerTime = notificationDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("reminder_id", reminder.id)
                    putExtra("reminder_title", reminder.title)
                    putExtra("reminder_description", reminder.description)
                    putExtra("reminder_amount", reminder.amount)
                    putExtra("reminder_currency", reminder.currency)
                    putExtra("days_before", daysBefore)
                }
                
                val requestCode = "${reminder.id}_$daysBefore".hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        }
        
        // Programar notificación para el día del vencimiento
        val dueDateNotification = LocalDateTime.of(reminder.dueDate, reminder.reminderTime)
        if (dueDateNotification.isAfter(LocalDateTime.now())) {
            val triggerTime = dueDateNotification
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("reminder_title", reminder.title)
                putExtra("reminder_description", reminder.description)
                putExtra("reminder_amount", reminder.amount)
                putExtra("reminder_currency", reminder.currency)
                putExtra("days_before", 0)
                putExtra("is_due_date", true)
            }
            
            val requestCode = "${reminder.id}_due".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelNotification(reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancelar todas las notificaciones programadas para este recordatorio
        listOf(3, 1, 0).forEach { daysBefore ->
            val requestCode = if (daysBefore == 0) {
                "${reminderId}_due".hashCode()
            } else {
                "${reminderId}_$daysBefore".hashCode()
            }
            
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
    }
    
    fun showNotification(
        id: Int,
        title: String,
        content: String,
        amount: Double? = null,
        currency: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val fullContent = buildString {
            append(content)
            if (amount != null && currency != null) {
                append("\nMonto: ${formatCurrency(amount, currency)}")
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(fullContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(id, notification)
    }
    
    private fun formatCurrency(amount: Double, currency: String): String {
        return when (currency) {
            "PYG" -> "${String.format("%,.0f", amount)} Gs."
            "USD" -> "$${String.format("%.2f", amount)}"
            else -> "${String.format("%.2f", amount)} $currency"
        }
    }
}

// app/src/main/java/com/py/ani_nderesarai/workers/AlarmReceiver.kt
package com.py.ani_nderesarai.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.py.ani_nderesarai.utils.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", 0)
        val title = intent.getStringExtra("reminder_title") ?: "Recordatorio de Pago"
        val description = intent.getStringExtra("reminder_description") ?: ""
        val amount = intent.getDoubleExtra("reminder_amount", 0.0)
        val currency = intent.getStringExtra("reminder_currency") ?: "PYG"
        val daysBefore = intent.getIntExtra("days_before", 0)
        val isDueDate = intent.getBooleanExtra("is_due_date", false)
        
        val notificationTitle = "🔔 $title"
        val notificationContent = when {
            isDueDate -> "¡VENCE HOY! $description"
            daysBefore == 1 -> "Vence mañana. $description"
            else -> "Vence en $daysBefore días. $description"
        }
        
        notificationManager.showNotification(
            id = reminderId.toInt(),
            title = notificationTitle,
            content = notificationContent,
            amount = if (amount > 0) amount else null,
            currency = currency
        )
    }
}

// app/src/main/java/com/py/ani_nderesarai/workers/ReminderBroadcastReceiver.kt
package com.py.ani_nderesarai.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
import com.py.ani_nderesarai.utils.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var repository: PaymentReminderRepository
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // Reprogramar todas las notificaciones después del reinicio
                CoroutineScope(Dispatchers.IO).launch {
                    rescheduleAllNotifications()
                }
            }
        }
    }
    
    private suspend fun rescheduleAllNotifications() {
        repository.getAllActiveReminders().collect { reminders ->
            reminders.forEach { reminder ->
                notificationManager.scheduleNotification(reminder)
            }
        }
    }
}
