package com.py.ani_nderesarai.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.py.ani_nderesarai.data.model.PaymentReminder

@Database(
    entities = [PaymentReminder::class],
    version = 2,  // ✅ INCREMENTADO de 1 a 2
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun paymentReminderDao(): PaymentReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ MIGRACIÓN de versión 1 a 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar nuevas columnas con valores por defecto

                // Sistema de cuotas
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN isInstallments INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN totalInstallments INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN currentInstallment INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN installmentInterval INTEGER NOT NULL DEFAULT 30")

                // Recurrencia personalizada
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN customRecurringDays INTEGER NOT NULL DEFAULT 30")

                // Estados y gestión
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN isCancelled INTEGER NOT NULL DEFAULT 0")

                // Fechas adicionales
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN createdAt TEXT NOT NULL DEFAULT '${java.time.LocalDate.now()}'")
                database.execSQL("ALTER TABLE payment_reminders ADD COLUMN lastModified TEXT NOT NULL DEFAULT '${java.time.LocalDate.now()}'")

                // Actualizar status basado en isActive existente
                database.execSQL("""
                    UPDATE payment_reminders 
                    SET status = CASE 
                        WHEN isActive = 1 THEN 'ACTIVE'
                        ELSE 'CANCELLED'
                    END
                """)

                // Crear índices para mejorar rendimiento
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payment_reminders_status ON payment_reminders(status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payment_reminders_dueDate ON payment_reminders(dueDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payment_reminders_category ON payment_reminders(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payment_reminders_priority ON payment_reminders(priority)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ani_nderesarai_database"
                )
                    .addMigrations(MIGRATION_1_2)  // ✅ Agregar migración
                    // .fallbackToDestructiveMigration() // ⚠️ COMENTADO - Solo para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Para desarrollo: Destruye la base de datos y crea una nueva
         * ⚠️ ELIMINAR EN PRODUCCIÓN - Perderás todos los datos
         */
        fun getDatabase_Dev(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ani_nderesarai_database"
                )
                    .fallbackToDestructiveMigration()  // ⚠️ Solo para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * INSTRUCCIONES DE MIGRACIÓN:
 *
 * 1. Si ya tienes la app instalada con datos:
 *    - Usa getDatabase() normal con la migración
 *    - Los datos existentes se preservarán
 *
 * 2. Si estás en desarrollo sin datos importantes:
 *    - Puedes usar getDatabase_Dev() temporalmente
 *    - O desinstalar la app y reinstalar
 *
 * 3. En DatabaseModule.kt, asegúrate de usar:
 *    AppDatabase.getDatabase(context)  // Con migración
 *
 * 4. Para verificar la migración:
 *    - Abre Database Inspector en Android Studio
 *    - Verifica que las nuevas columnas existan
 */