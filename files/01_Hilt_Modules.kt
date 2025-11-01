// app/src/main/java/com/py/ani_nderesarai/di/DatabaseModule.kt
package com.py.ani_nderesarai.di

import android.content.Context
import androidx.room.Room
import com.py.ani_nderesarai.data.database.AppDatabase
import com.py.ani_nderesarai.data.database.PaymentReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ani_nderesarai_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun providePaymentReminderDao(database: AppDatabase): PaymentReminderDao {
        return database.paymentReminderDao()
    }
}

// app/src/main/java/com/py/ani_nderesarai/di/AppModule.kt
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
}
