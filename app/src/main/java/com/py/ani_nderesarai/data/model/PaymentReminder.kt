package com.py.ani_nderesarai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "payment_reminders")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: PaymentCategory,
    val amount: Double? = null,
    val currency: String = "PYG",
    val dueDate: LocalDate,
    val reminderTime: LocalTime = LocalTime.of(9, 0),
    val isRecurring: Boolean = false,
    val recurringType: RecurringType = RecurringType.MONTHLY,
    val reminderDaysBefore: List<Int> = listOf(3, 1), // Días antes para recordar
    val whatsappNumber: String = "", // Número para envío por WhatsApp
    val isActive: Boolean = true,
    val customMessage: String = "", // Mensaje personalizado
    val iconType: PaymentIconType = PaymentIconType.GENERIC,
    val color: String = "#2196F3", // Color personalizable
    val priority: Priority = Priority.MEDIUM,
    val tags: List<String> = emptyList(),
    val lastPaid: LocalDate? = null,
    val notes: String = ""
)

enum class PaymentCategory {
    UTILITIES, // Servicios públicos
    LOANS, // Préstamos
    CREDIT_CARDS, // Tarjetas de crédito
    INSURANCE, // Seguros
    RENT, // Alquiler
    SUBSCRIPTIONS, // Suscripciones
    TAXES, // Impuestos
    EDUCATION, // Educación
    HEALTH, // Salud
    OTHER // Otros
}

enum class RecurringType {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    SEMI_ANNUAL,
    ANNUAL,
    CUSTOM
}

enum class PaymentIconType {
    GENERIC,
    WATER,
    ELECTRICITY,
    GAS,
    INTERNET,
    PHONE,
    CREDIT_CARD,
    BANK,
    HOUSE,
    CAR,
    HEALTH,
    EDUCATION,
    SHOPPING,
    ENTERTAINMENT
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}