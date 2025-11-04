package com.py.ani_nderesarai.data.repository

import com.py.ani_nderesarai.data.database.PaymentReminderDao
import com.py.ani_nderesarai.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentReminderRepository @Inject constructor(
    private val dao: PaymentReminderDao
) {

    // ============================================
    // CONSULTAS BÁSICAS
    // ============================================

    fun getAllActiveReminders(): Flow<List<PaymentReminder>> =
        dao.getAllActiveReminders()

    suspend fun getReminderById(id: Long): PaymentReminder? =
        dao.getReminderById(id)

    suspend fun insertReminder(reminder: PaymentReminder): Long =
        dao.insertReminder(reminder.copy(
            createdAt = LocalDate.now(),
            lastModified = LocalDate.now()
        ))

    suspend fun updateReminder(reminder: PaymentReminder) =
        dao.updateReminder(reminder.copy(
            lastModified = LocalDate.now()
        ))

    suspend fun deleteReminder(reminder: PaymentReminder) =
        dao.deleteReminder(reminder)

    // ============================================
    // CONSULTAS POR ESTADO
    // ============================================

    fun getPaidReminders(): Flow<List<PaymentReminder>> =
        dao.getPaidReminders()

    fun getCancelledReminders(): Flow<List<PaymentReminder>> =
        dao.getCancelledReminders()

    fun getOverdueReminders(): Flow<List<PaymentReminder>> =
        dao.getOverdueReminders()

    /**
     * Obtiene recordatorios próximos en los siguientes X días
     */
    fun getUpcomingReminders(daysAhead: Int = 7): Flow<List<PaymentReminder>> {
        val today = LocalDate.now()
        val endDate = today.plusDays(daysAhead.toLong())
        return dao.getRemindersByDateRange(today, endDate)
    }

    // ============================================
    // FILTROS Y BÚSQUEDA
    // ============================================

    fun getRemindersByCategory(category: PaymentCategory): Flow<List<PaymentReminder>> =
        dao.getRemindersByCategory(category)

    fun getRemindersByPriority(priority: Priority): Flow<List<PaymentReminder>> =
        dao.getRemindersByPriority(priority)

    fun searchReminders(query: String): Flow<List<PaymentReminder>> =
        dao.searchReminders(query)

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    fun getFilteredReminders(
        filters: ReminderFilters,
        orderBy: String = "DATE_ASC"
    ): Flow<List<PaymentReminder>> =
        dao.getFilteredReminders(
            status = filters.status,
            category = filters.category,
            priority = filters.priority,
            minAmount = filters.minAmount,
            maxAmount = filters.maxAmount,
            dateFrom = filters.dateFrom,
            dateTo = filters.dateTo,
            searchQuery = filters.searchQuery,
            orderBy = orderBy
        )

    // ============================================
    // GESTIÓN DE ESTADOS
    // ============================================

    /**
     * Marca un recordatorio como pagado
     * Si es recurrente, crea el siguiente recordatorio
     * Si es en cuotas, avanza a la siguiente cuota
     */
    suspend fun markAsPaid(reminder: PaymentReminder) {
        when {
            // Pago en cuotas
            reminder.isInstallments -> {
                if (reminder.currentInstallment < reminder.totalInstallments) {
                    // Avanzar a la siguiente cuota
                    val nextDueDate = reminder.dueDate.plusDays(reminder.installmentInterval.toLong())
                    dao.updateReminder(
                        reminder.copy(
                            currentInstallment = reminder.currentInstallment + 1,
                            dueDate = nextDueDate,
                            lastPaid = LocalDate.now(),
                            status = if (reminder.currentInstallment + 1 == reminder.totalInstallments)
                                ReminderStatus.PAID
                            else
                                ReminderStatus.PARTIAL
                        )
                    )
                } else {
                    // Última cuota - marcar como pagado completamente
                    dao.markAsPaid(reminder.id)
                }
            }

            // Pago recurrente
            reminder.isRecurring -> {
                // Marcar actual como pagado
                dao.markAsPaid(reminder.id)
                // Crear siguiente recordatorio
                scheduleNextRecurringReminder(reminder)
            }

            // Pago único
            else -> {
                dao.markAsPaid(reminder.id)
            }
        }
    }

    /**
     * Cancela un recordatorio (lo mueve a cancelados)
     */
    suspend fun cancelReminder(id: Long) {
        dao.markAsCancelled(id)
    }

    /**
     * Reactiva un recordatorio cancelado
     */
    suspend fun reactivateReminder(id: Long) {
        dao.reactivateReminder(id)
    }

    // ============================================
    // CUOTAS
    // ============================================

    fun getInstallmentReminders(): Flow<List<PaymentReminder>> =
        dao.getInstallmentReminders()

    /**
     * Crea un recordatorio con sistema de cuotas
     */
    suspend fun createInstallmentReminder(
        baseReminder: PaymentReminder,
        totalInstallments: Int,
        intervalDays: Int
    ): Long {
        val installmentReminder = baseReminder.copy(
            isInstallments = true,
            totalInstallments = totalInstallments,
            currentInstallment = 1,
            installmentInterval = intervalDays,
            amount = baseReminder.amount?.div(totalInstallments) // Dividir monto por cuotas
        )
        return dao.insertReminder(installmentReminder)
    }

    // ============================================
    // RECURRENCIA
    // ============================================

    /**
     * Programa el siguiente recordatorio recurrente
     */
    suspend fun scheduleNextRecurringReminder(reminder: PaymentReminder) {
        if (!reminder.isRecurring) return

        val nextDueDate = calculateNextDueDate(reminder)
        val nextReminder = reminder.copy(
            id = 0, // Nuevo ID (autoincrement)
            dueDate = nextDueDate,
            lastPaid = null,
            isPaid = false,
            status = ReminderStatus.ACTIVE,
            createdAt = LocalDate.now(),
            lastModified = LocalDate.now()
        )

        dao.insertReminder(nextReminder)
    }

    private fun calculateNextDueDate(reminder: PaymentReminder): LocalDate {
        return when (reminder.recurringType) {
            RecurringType.DAILY -> reminder.dueDate.plusDays(1)
            RecurringType.WEEKLY -> reminder.dueDate.plusWeeks(1)
            RecurringType.BIWEEKLY -> reminder.dueDate.plusWeeks(2)
            RecurringType.MONTHLY -> reminder.dueDate.plusMonths(1)
            RecurringType.BIMONTHLY -> reminder.dueDate.plusMonths(2)
            RecurringType.QUARTERLY -> reminder.dueDate.plusMonths(3)
            RecurringType.SEMI_ANNUAL -> reminder.dueDate.plusMonths(6)
            RecurringType.ANNUAL -> reminder.dueDate.plusYears(1)
            RecurringType.CUSTOM -> reminder.dueDate.plusDays(reminder.customRecurringDays.toLong())
        }
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    suspend fun getStatistics(): ReminderStatistics {
        return ReminderStatistics(
            activeCount = dao.getActiveCount(),
            overdueCount = dao.getOverdueCount(),
            paidCount = dao.getPaidCount(),
            totalPendingAmount = dao.getTotalPendingAmount() ?: 0.0
        )
    }

    suspend fun getTotalPaidInMonth(month: Int, year: Int): Double {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return dao.getTotalPaidInPeriod(startDate, endDate) ?: 0.0
    }

    // ============================================
    // MANTENIMIENTO
    // ============================================

    /**
     * Actualiza estados de recordatorios vencidos
     */
    suspend fun updateOverdueStatus() {
        dao.updateOverdueStatus()
    }

    /**
     * Limpia recordatorios cancelados antiguos (más de 90 días)
     */
    suspend fun cleanupOldCancelledReminders(daysOld: Int = 90) {
        val beforeDate = LocalDate.now().minusDays(daysOld.toLong())
        dao.deleteOldCancelledReminders(beforeDate)
    }

    // ============================================
    // OBTENER RECORDATORIOS PARA EL BOT
    // ============================================

    /**
     * Obtiene recordatorios que el bot debe enviar
     * (próximos X días, activos)
     */
    suspend fun getRemindersForBot(daysAhead: Int): List<PaymentReminder> {
        val today = LocalDate.now()
        val endDate = today.plusDays(daysAhead.toLong())

        return dao.getRemindersByDateRange(today, endDate)
            .map { reminders ->
                reminders.filter { it.status == ReminderStatus.ACTIVE && it.whatsappNumber.isNotBlank() }
            }
            .map { it }
            .let { flow ->
                // Convertir Flow a List (tomar primer valor)
                var result: List<PaymentReminder> = emptyList()
                flow.collect { result = it }
                result
            }
    }
}

// ✅ Data class para estadísticas
data class ReminderStatistics(
    val activeCount: Int,
    val overdueCount: Int,
    val paidCount: Int,
    val totalPendingAmount: Double
)