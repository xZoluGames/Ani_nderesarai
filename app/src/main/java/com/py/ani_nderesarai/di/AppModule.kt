package com.py.ani_nderesarai.di

import android.content.Context
import com.py.ani_nderesarai.utils.NotificationManager
import com.py.ani_nderesarai.utils.WhatsAppManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWhatsAppManager(): WhatsAppManager {
        return WhatsAppManager()
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return NotificationManager(context)
    }

    // ✅ WhatsAppBotManager REMOVIDO
    // El bot ahora funciona a través de la API REST en la VPS
    // No se necesita un manager local
}