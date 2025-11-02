package com.py.ani_nderesarai.di

import android.content.Context
import androidx.room.Room
import com.py.ani_nderesarai.data.database.AppDatabase
import com.py.ani_nderesarai.data.database.PaymentReminderDao
import com.py.ani_nderesarai.data.repository.PaymentReminderRepository
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
        )
            .fallbackToDestructiveMigration() // Para desarrollo, eliminar en producción
            .build()
    }

    @Provides
    @Singleton
    fun providePaymentReminderDao(database: AppDatabase): PaymentReminderDao {
        return database.paymentReminderDao()
    }

    @Provides
    @Singleton
    fun providePaymentReminderRepository(dao: PaymentReminderDao): PaymentReminderRepository {
        return PaymentReminderRepository(dao)
    }
}