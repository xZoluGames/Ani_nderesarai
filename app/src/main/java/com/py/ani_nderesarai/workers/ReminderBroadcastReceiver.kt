package com.py.ani_nderesarai.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.py.ani_nderesarai.data.database.AppDatabase
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
import com.py.ani_nderesarai.utils.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // Programar un Worker para reprogramar las notificaciones
                scheduleNotificationWorker(context)
            }
        }
    }

    private fun scheduleNotificationWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reschedule_notifications",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}

// Worker separado para manejar la reprogramación
class RescheduleNotificationsWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = PaymentReminderRepository(database.paymentReminderDao())
            val notificationManager = NotificationManager(applicationContext)

            // Usar coroutines para obtener los recordatorios
            CoroutineScope(Dispatchers.IO).launch {
                repository.getAllActiveReminders().collect { reminders ->
                    reminders.forEach { reminder ->
                        notificationManager.scheduleNotification(reminder)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}