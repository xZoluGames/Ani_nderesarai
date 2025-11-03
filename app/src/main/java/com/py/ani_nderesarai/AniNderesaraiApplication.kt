package com.py.ani_nderesarai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.py.ani_nderesarai.utils.WhatsAppBotManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AniNderesaraiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Verificar y reprogramar el bot si estaba activado
        checkAndRestartBot()
    }

    private fun checkAndRestartBot() {
        try {
            val botManager = WhatsAppBotManager(this)
            if (botManager.isBotEnabled()) {
                val status = botManager.getBotStatus()
                if (status.phoneNumber.isNotBlank()) {
                    botManager.scheduleBot(
                        phoneNumber = status.phoneNumber,
                        hour = status.sendTime.first,
                        minute = status.sendTime.second,
                        daysAhead = status.daysAhead
                    )
                }
            }
        } catch (e: Exception) {
            // Log pero no crashear si hay error al inicializar el bot
            e.printStackTrace()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}