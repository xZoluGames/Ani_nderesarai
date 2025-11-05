package com.py.ani_nderesarai.data.model

/**
 * Data classes auxiliares para queries y estadísticas
 */

/**
 * Resultado de estadísticas por categoría
 */
data class CategoryStatistic(
    val category: PaymentCategory,
    val count: Int
)

/**
 * Resumen de estadísticas generales
 */
data class ReminderStatistics(
    val activeCount: Int = 0,
    val overdueCount: Int = 0,
    val paidCount: Int = 0,
    val totalPendingAmount: Double = 0.0
)

/**
 * Filtros para búsqueda avanzada
 */
data class ReminderFilters(
    val status: ReminderStatus? = null,
    val category: PaymentCategory? = null,
    val priority: Priority? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val dateFrom: java.time.LocalDate? = null,
    val dateTo: java.time.LocalDate? = null,
    val searchQuery: String = ""
)