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