package com.py.ani_nderesarai.data.database

import androidx.room.*
import com.py.ani_nderesarai.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PaymentReminderDao {

    // ============================================
    // CONSULTAS BÁSICAS
    // ============================================

    @Query("SELECT * FROM payment_reminders WHERE status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getAllActiveReminders(): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): PaymentReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: PaymentReminder): Long

    @Update
    suspend fun updateReminder(reminder: PaymentReminder)

    @Delete
    suspend fun deleteReminder(reminder: PaymentReminder)

    // ============================================
    // CONSULTAS POR ESTADO
    // ============================================

    @Query("SELECT * FROM payment_reminders WHERE status = :status ORDER BY dueDate ASC")
    fun getRemindersByStatus(status: ReminderStatus): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE isPaid = 1 ORDER BY lastPaid DESC")
    fun getPaidReminders(): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE isCancelled = 1 ORDER BY lastModified DESC")
    fun getCancelledReminders(): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE status = 'ACTIVE' AND dueDate < :today ORDER BY dueDate ASC")
    fun getOverdueReminders(today: LocalDate = LocalDate.now()): Flow<List<PaymentReminder>>

    // ============================================
    // CONSULTAS POR FILTROS
    // ============================================

    @Query("SELECT * FROM payment_reminders WHERE category = :category AND status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getRemindersByCategory(category: PaymentCategory): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE priority = :priority AND status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getRemindersByPriority(priority: Priority): Flow<List<PaymentReminder>>

    @Query("""
        SELECT * FROM payment_reminders 
        WHERE status = 'ACTIVE' 
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        ORDER BY dueDate ASC
    """)
    fun getRemindersByAmountRange(minAmount: Double?, maxAmount: Double?): Flow<List<PaymentReminder>>

    @Query("""
        SELECT * FROM payment_reminders 
        WHERE dueDate BETWEEN :startDate AND :endDate 
        AND status = 'ACTIVE'
        ORDER BY dueDate ASC
    """)
    fun getRemindersByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PaymentReminder>>

    @Query("""
        SELECT * FROM payment_reminders 
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND status = 'ACTIVE'
        ORDER BY dueDate ASC
    """)
    fun searchReminders(query: String): Flow<List<PaymentReminder>>

    // ============================================
    // BÚSQUEDA AVANZADA CON MÚLTIPLES FILTROS
    // ============================================

    @Query("""
        SELECT * FROM payment_reminders 
        WHERE 1=1
        AND (:status IS NULL OR status = :status)
        AND (:category IS NULL OR category = :category)
        AND (:priority IS NULL OR priority = :priority)
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        AND (:dateFrom IS NULL OR dueDate >= :dateFrom)
        AND (:dateTo IS NULL OR dueDate <= :dateTo)
        AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY 
            CASE WHEN :orderBy = 'DATE_ASC' THEN dueDate END ASC,
            CASE WHEN :orderBy = 'DATE_DESC' THEN dueDate END DESC,
            CASE WHEN :orderBy = 'AMOUNT_ASC' THEN amount END ASC,
            CASE WHEN :orderBy = 'AMOUNT_DESC' THEN amount END DESC,
            CASE WHEN :orderBy = 'PRIORITY' THEN 
                CASE priority 
                    WHEN 'URGENT' THEN 1
                    WHEN 'HIGH' THEN 2
                    WHEN 'MEDIUM' THEN 3
                    WHEN 'LOW' THEN 4
                END
            END ASC
    """)
    fun getFilteredReminders(
        status: ReminderStatus?,
        category: PaymentCategory?,
        priority: Priority?,
        minAmount: Double?,
        maxAmount: Double?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        searchQuery: String,
        orderBy: String = "DATE_ASC"
    ): Flow<List<PaymentReminder>>

    // ============================================
    // GESTIÓN DE ESTADOS
    // ============================================

    @Query("UPDATE payment_reminders SET status = :status, lastModified = :date WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ReminderStatus, date: LocalDate = LocalDate.now())

    @Query("""
        UPDATE payment_reminders 
        SET isPaid = 1, 
            status = 'PAID', 
            lastPaid = :paidDate,
            lastModified = :paidDate
        WHERE id = :id
    """)
    suspend fun markAsPaid(id: Long, paidDate: LocalDate = LocalDate.now())

    @Query("""
        UPDATE payment_reminders 
        SET isCancelled = 1, 
            status = 'CANCELLED',
            isActive = 0,
            lastModified = :date
        WHERE id = :id
    """)
    suspend fun markAsCancelled(id: Long, date: LocalDate = LocalDate.now())

    @Query("""
        UPDATE payment_reminders 
        SET status = 'ACTIVE',
            isPaid = 0,
            isCancelled = 0,
            isActive = 1,
            lastModified = :date
        WHERE id = :id
    """)
    suspend fun reactivateReminder(id: Long, date: LocalDate = LocalDate.now())

    // ============================================
    // CUOTAS
    // ============================================

    @Query("SELECT * FROM payment_reminders WHERE isInstallments = 1 AND status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getInstallmentReminders(): Flow<List<PaymentReminder>>

    @Query("""
        UPDATE payment_reminders 
        SET currentInstallment = currentInstallment + 1,
            lastModified = :date
        WHERE id = :id
    """)
    suspend fun advanceInstallment(id: Long, date: LocalDate = LocalDate.now())

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    @Query("SELECT COUNT(*) FROM payment_reminders WHERE status = 'ACTIVE'")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM payment_reminders WHERE status = 'ACTIVE' AND dueDate < :today")
    suspend fun getOverdueCount(today: LocalDate = LocalDate.now()): Int

    @Query("SELECT COUNT(*) FROM payment_reminders WHERE isPaid = 1")
    suspend fun getPaidCount(): Int

    @Query("SELECT SUM(amount) FROM payment_reminders WHERE status = 'ACTIVE' AND amount IS NOT NULL")
    suspend fun getTotalPendingAmount(): Double?

    @Query("""
        SELECT SUM(amount) FROM payment_reminders 
        WHERE isPaid = 1 
        AND lastPaid BETWEEN :startDate AND :endDate
        AND amount IS NOT NULL
    """)
    suspend fun getTotalPaidInPeriod(startDate: LocalDate, endDate: LocalDate): Double?

    @Query("""
        SELECT category, COUNT(*) as count 
        FROM payment_reminders 
        WHERE status = 'ACTIVE' 
        GROUP BY category 
        ORDER BY count DESC
    """)
    suspend fun getCategoryStatistics(): Map<PaymentCategory, Int>

    // ============================================
    // LIMPIEZA Y MANTENIMIENTO
    // ============================================

    @Query("DELETE FROM payment_reminders WHERE isCancelled = 1 AND lastModified < :beforeDate")
    suspend fun deleteOldCancelledReminders(beforeDate: LocalDate)

    @Query("""
        UPDATE payment_reminders 
        SET status = 'OVERDUE' 
        WHERE status = 'ACTIVE' 
        AND dueDate < :today
        AND isPaid = 0
    """)
    suspend fun updateOverdueStatus(today: LocalDate = LocalDate.now())
}