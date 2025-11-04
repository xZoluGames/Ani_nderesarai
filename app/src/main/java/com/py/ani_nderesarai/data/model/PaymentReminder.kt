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
    val isInstallments: Boolean = false,  // ✅ NUEVO: Indica si es pago en cuotas
    val totalInstallments: Int = 1,        // ✅ NUEVO: Total de cuotas
    val currentInstallment: Int = 1,       // ✅ NUEVO: Cuota actual
    val installmentInterval: Int = 30,     // ✅ NUEVO: Días entre cuotas (default 30 = mensual)

    // Recurrencia
    val isRecurring: Boolean = false,
    val recurringType: RecurringType = RecurringType.MONTHLY,
    val customRecurringDays: Int = 30,    // ✅ NUEVO: Para recurrencias personalizadas
    val reminderDaysBefore: List<Int> = listOf(3, 1),

    // WhatsApp
    val whatsappNumber: String = "",
    val customMessage: String = "",

    // Estado y gestión
    val status: ReminderStatus = ReminderStatus.ACTIVE,  // ✅ NUEVO: Estado del recordatorio
    val isPaid: Boolean = false,           // ✅ NUEVO: Marcador rápido de pagado
    val isCancelled: Boolean = false,      // ✅ NUEVO: Marcador de cancelado
    val isActive: Boolean = true,          // Mantener para compatibilidad

    // Personalización
    val iconType: PaymentIconType = PaymentIconType.GENERIC,
    val color: String = "#2196F3",
    val priority: Priority = Priority.MEDIUM,
    val tags: List<String> = emptyList(),
    val notes: String = ""
)

// ✅ NUEVO: Enum para estados del recordatorio
enum class ReminderStatus {
    ACTIVE,      // Activo, pendiente de pago
    PAID,        // Pagado completamente
    CANCELLED,   // Cancelado/Abandonado
    OVERDUE,     // Vencido (calculado dinámicamente)
    PARTIAL      // Parcialmente pagado (para cuotas)
}

enum class PaymentCategory {
    UTILITIES,       // Servicios públicos
    WATER,          // ✅ NUEVO: Agua específicamente
    ELECTRICITY,    // ✅ NUEVO: Luz específicamente
    GAS,            // ✅ NUEVO: Gas específicamente
    INTERNET,       // ✅ NUEVO: Internet específicamente
    PHONE,          // ✅ NUEVO: Teléfono específicamente
    LOANS,          // Préstamos
    CREDIT_CARDS,   // Tarjetas de crédito
    INSURANCE,      // Seguros
    RENT,           // Alquiler
    SUBSCRIPTIONS,  // Suscripciones
    TAXES,          // Impuestos
    EDUCATION,      // Educación
    HEALTH,         // Salud
    ENTERTAINMENT,  // ✅ NUEVO: Entretenimiento
    TRANSPORT,      // ✅ NUEVO: Transporte
    OTHER           // Otros
}

enum class RecurringType {
    DAILY,          // ✅ NUEVO: Diario
    WEEKLY,         // Semanal
    BIWEEKLY,       // ✅ NUEVO: Quincenal
    MONTHLY,        // Mensual
    BIMONTHLY,      // ✅ NUEVO: Bimestral (cada 2 meses)
    QUARTERLY,      // Trimestral (cada 3 meses)
    SEMI_ANNUAL,    // Semestral (cada 6 meses)
    ANNUAL,         // Anual
    CUSTOM          // Personalizado
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
    ENTERTAINMENT,
    TRANSPORT,      // ✅ NUEVO
    SUBSCRIPTION,   // ✅ NUEVO
    INSURANCE,      // ✅ NUEVO
    TAX             // ✅ NUEVO
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

// ✅ NUEVO: Data class para filtros
data class ReminderFilters(
    val status: ReminderStatus? = null,
    val category: PaymentCategory? = null,
    val priority: Priority? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val searchQuery: String = ""
)