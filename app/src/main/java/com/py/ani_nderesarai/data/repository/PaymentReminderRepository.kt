// app/src/main/java/com/py/ani_nderesarai/data/repository/PaymentReminderRepository.kt
package com.py.ani_nderesarai.data.repository

import com.py.ani_nderesarai.data.database.PaymentReminderDao
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.data.model.PaymentCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentReminderRepository @Inject constructor(
    private val dao: PaymentReminderDao
) {
    fun getAllActiveReminders(): Flow<List<PaymentReminder>> = 
        dao.getAllActiveReminders()
    
    suspend fun getReminderById(id: Long): PaymentReminder? = 
        dao.getReminderById(id)
    
    fun getRemindersByCategory(category: PaymentCategory): Flow<List<PaymentReminder>> =
        dao.getRemindersByCategory(category)
    
    fun getRemindersByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PaymentReminder>> =
        dao.getRemindersByDateRange(startDate, endDate)
    
    suspend fun getUpcomingReminders(date: LocalDate): List<PaymentReminder> =
        dao.getUpcomingReminders(date)
    
    suspend fun insertReminder(reminder: PaymentReminder): Long =
        dao.insertReminder(reminder)
    
    suspend fun updateReminder(reminder: PaymentReminder) =
        dao.updateReminder(reminder)
    
    suspend fun deleteReminder(reminder: PaymentReminder) =
        dao.deleteReminder(reminder)
    
    suspend fun deactivateReminder(id: Long) =
        dao.deactivateReminder(id)
    
    suspend fun markAsPaid(id: Long, paidDate: LocalDate = LocalDate.now()) =
        dao.markAsPaid(id, paidDate)
    
    suspend fun scheduleNextRecurringReminder(reminder: PaymentReminder) {
        if (reminder.isRecurring) {
            val nextDueDate = calculateNextDueDate(reminder)
            val updatedReminder = reminder.copy(
                dueDate = nextDueDate,
                lastPaid = reminder.dueDate
            )
            dao.insertReminder(updatedReminder)
        }
    }
    
    private fun calculateNextDueDate(reminder: PaymentReminder): LocalDate {
        return when (reminder.recurringType) {
            com.py.ani_nderesarai.data.model.RecurringType.WEEKLY -> reminder.dueDate.plusWeeks(1)
            com.py.ani_nderesarai.data.model.RecurringType.MONTHLY -> reminder.dueDate.plusMonths(1)
            com.py.ani_nderesarai.data.model.RecurringType.QUARTERLY -> reminder.dueDate.plusMonths(3)
            com.py.ani_nderesarai.data.model.RecurringType.SEMI_ANNUAL -> reminder.dueDate.plusMonths(6)
            com.py.ani_nderesarai.data.model.RecurringType.ANNUAL -> reminder.dueDate.plusYears(1)
            com.py.ani_nderesarai.data.model.RecurringType.CUSTOM -> reminder.dueDate.plusMonths(1) // Por defecto mensual
        }
    }
}