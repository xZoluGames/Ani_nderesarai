package com.py.ani_nderesarai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "payment_reminders")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Información básica
    val title: String,
    val description: String = "",
    val category: PaymentCategory,
    val amount: Double? = null,
    val currency: String = "PYG",

    // Fechas y horarios
    val dueDate: LocalDate,
    val reminderTime: LocalTime = LocalTime.of(9, 0),
    val createdAt: LocalDate = LocalDate.now(),
    val lastModified: LocalDate = LocalDate.now(),
    val lastPaid: LocalDate? = null,

    // Sistema de cuotas
    val isInstallments: Boolean = false,
    val totalInstallments: Int = 1,
    val currentInstallment: Int = 1,
    val installmentInterval: Int = 30,

    // Recurrencia
    val isRecurring: Boolean = false,
    val recurringType: RecurringType = RecurringType.MONTHLY,
    val customRecurringDays: Int = 30,
    val reminderDaysBefore: List<Int> = listOf(3, 1),

    // WhatsApp
    val whatsappNumber: String = "",
    val customMessage: String = "",

    // Estado y gestión
    val status: ReminderStatus = ReminderStatus.ACTIVE,
    val isPaid: Boolean = false,
    val isCancelled: Boolean = false,
    val isActive: Boolean = true,

    // Personalización
    val iconType: PaymentIconType = PaymentIconType.GENERIC,
    val color: String = "#2196F3",
    val priority: Priority = Priority.MEDIUM,
    val tags: List<String> = emptyList(),
    val notes: String = ""
)

// Estados del recordatorio
enum class ReminderStatus {
    ACTIVE,
    PAID,
    CANCELLED,
    OVERDUE,
    PARTIAL
}

// Categorías de pago
enum class PaymentCategory {
    UTILITIES,
    WATER,
    ELECTRICITY,
    GAS,
    INTERNET,
    PHONE,
    LOANS,
    CREDIT_CARDS,
    INSURANCE,
    RENT,
    SUBSCRIPTIONS,
    TAXES,
    EDUCATION,
    HEALTH,
    ENTERTAINMENT,
    TRANSPORT,
    OTHER
}

// Tipos de recurrencia
enum class RecurringType {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    BIMONTHLY,
    QUARTERLY,
    SEMI_ANNUAL,
    ANNUAL,
    CUSTOM
}

// Tipos de iconos
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
    ENTERTAINMENT,
    TRANSPORT,
    SUBSCRIPTION,
    INSURANCE,
    TAX
}

// Prioridades
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}