package com.py.ani_nderesarai.data.database

import androidx.room.*
import com.py.ani_nderesarai.data.model.PaymentReminder
import com.py.ani_nderesarai.data.model.PaymentCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PaymentReminderDao {
    
    @Query("SELECT * FROM payment_reminders WHERE isActive = 1 ORDER BY dueDate ASC")
    fun getAllActiveReminders(): Flow<List<PaymentReminder>>
    
    @Query("SELECT * FROM payment_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): PaymentReminder?
    
    @Query("SELECT * FROM payment_reminders WHERE category = :category AND isActive = 1")
    fun getRemindersByCategory(category: PaymentCategory): Flow<List<PaymentReminder>>
    
    @Query("SELECT * FROM payment_reminders WHERE dueDate BETWEEN :startDate AND :endDate AND isActive = 1")
    fun getRemindersByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PaymentReminder>>
    
    @Query("SELECT * FROM payment_reminders WHERE dueDate <= :date AND isActive = 1")
    suspend fun getUpcomingReminders(date: LocalDate): List<PaymentReminder>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: PaymentReminder): Long
    
    @Update
    suspend fun updateReminder(reminder: PaymentReminder)
    
    @Delete
    suspend fun deleteReminder(reminder: PaymentReminder)
    
    @Query("UPDATE payment_reminders SET isActive = 0 WHERE id = :id")
    suspend fun deactivateReminder(id: Long)
    
    @Query("UPDATE payment_reminders SET lastPaid = :paidDate WHERE id = :id")
    suspend fun markAsPaid(id: Long, paidDate: LocalDate)
}