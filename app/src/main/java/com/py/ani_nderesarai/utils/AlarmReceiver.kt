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