package com.py.ani_nderesarai.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class AppUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // La app fue actualizada, reprogramar notificaciones
            scheduleNotificationWorker(context)
        }
    }

    private fun scheduleNotificationWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reschedule_after_update",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}