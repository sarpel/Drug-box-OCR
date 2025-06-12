package com.boxocr.simple.database

import androidx.room.*
import com.boxocr.simple.data.MultiDrugSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced DAO for Multi-Drug Session Management
 * Comprehensive database operations for session lifecycle management
 */

@Dao
interface MultiDrugSessionDao {

    // Basic CRUD Operations

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MultiDrugSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<MultiDrugSessionEntity>)

    @Update
    suspend fun updateSession(session: MultiDrugSessionEntity)

    @Delete
    suspend fun deleteSession(session: MultiDrugSessionEntity)

    @Query("DELETE FROM multi_drug_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM multi_drug_sessions")
    suspend fun deleteAllSessions()

    // Query Operations

    @Query("SELECT * FROM multi_drug_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): MultiDrugSessionEntity?

    @Query("SELECT * FROM multi_drug_sessions WHERE sessionId = :sessionId")
    fun getSessionByIdFlow(sessionId: String): Flow<MultiDrugSessionEntity?>

    @Query("SELECT * FROM multi_drug_sessions ORDER BY lastUpdated DESC")
    suspend fun getAllSessions(): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions ORDER BY lastUpdated DESC")
    fun getAllSessionsFlow(): Flow<List<MultiDrugSessionEntity>>

    @Query("SELECT * FROM multi_drug_sessions WHERE status = :status ORDER BY lastUpdated DESC")
    suspend fun getSessionsByStatus(status: String): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE status = :status ORDER BY lastUpdated DESC")
    fun getSessionsByStatusFlow(status: String): Flow<List<MultiDrugSessionEntity>>

    @Query("SELECT * FROM multi_drug_sessions WHERE status != 'COMPLETED' ORDER BY lastUpdated DESC")
    suspend fun getActiveSessions(): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE status != 'COMPLETED' ORDER BY lastUpdated DESC")
    fun getActiveSessionsFlow(): Flow<List<MultiDrugSessionEntity>>

    @Query("SELECT * FROM multi_drug_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    suspend fun getCompletedSessions(): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedSessionsFlow(): Flow<List<MultiDrugSessionEntity>>

    // Date Range Queries

    @Query("SELECT * FROM multi_drug_sessions WHERE createdAt BETWEEN :startTime AND :endTime ORDER BY createdAt DESC")
    suspend fun getSessionsByDateRange(startTime: Long, endTime: Long): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE completedAt BETWEEN :startTime AND :endTime ORDER BY completedAt DESC")
    suspend fun getCompletedSessionsByDateRange(startTime: Long, endTime: Long): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE createdAt >= :startTime ORDER BY createdAt DESC")
    suspend fun getSessionsSince(startTime: Long): List<MultiDrugSessionEntity>

    // Search Operations

    @Query("SELECT * FROM multi_drug_sessions WHERE sessionName LIKE '%' || :query || '%' ORDER BY lastUpdated DESC")
    suspend fun searchSessionsByName(query: String): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE sessionName LIKE '%' || :query || '%' ORDER BY lastUpdated DESC")
    fun searchSessionsByNameFlow(query: String): Flow<List<MultiDrugSessionEntity>>

    @Query("""
        SELECT * FROM multi_drug_sessions 
        WHERE sessionName LIKE '%' || :query || '%' 
        OR patientInfo LIKE '%' || :query || '%'
        ORDER BY lastUpdated DESC
    """)
    suspend fun searchSessions(query: String): List<MultiDrugSessionEntity>

    // Statistics and Analytics Queries

    @Query("SELECT COUNT(*) FROM multi_drug_sessions")
    suspend fun getTotalSessionCount(): Int

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE status = :status")
    suspend fun getSessionCountByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE status != 'COMPLETED'")
    suspend fun getActiveSessionCount(): Int

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE status = 'COMPLETED'")
    suspend fun getCompletedSessionCount(): Int

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE createdAt >= :startTime")
    suspend fun getSessionCountSince(startTime: Long): Int

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE createdAt BETWEEN :startTime AND :endTime")
    suspend fun getSessionCountInRange(startTime: Long, endTime: Long): Int

    @Query("""
        SELECT AVG(completedAt - createdAt) 
        FROM multi_drug_sessions 
        WHERE status = 'COMPLETED' AND completedAt IS NOT NULL
    """)
    suspend fun getAverageSessionDuration(): Long?

    @Query("""
        SELECT MIN(createdAt) as minTime, MAX(createdAt) as maxTime, COUNT(*) as totalCount
        FROM multi_drug_sessions
    """)
    suspend fun getSessionTimeRange(): SessionTimeRange?

    // Maintenance and Cleanup Operations

    @Query("DELETE FROM multi_drug_sessions WHERE completedAt < :cutoffTime AND status = 'COMPLETED'")
    suspend fun deleteOldCompletedSessions(cutoffTime: Long): Int

    @Query("DELETE FROM multi_drug_sessions WHERE createdAt < :cutoffTime AND status IN ('ERROR', 'CANCELLED')")
    suspend fun deleteOldFailedSessions(cutoffTime: Long): Int

    @Query("""
        UPDATE multi_drug_sessions 
        SET status = 'ERROR' 
        WHERE status IN ('SCANNING', 'VERIFICATION', 'BATCH_PROCESSING') 
        AND lastUpdated < :cutoffTime
    """)
    suspend fun markStaleSessionsAsError(cutoffTime: Long): Int

    // Custom Update Operations

    @Query("UPDATE multi_drug_sessions SET status = :status, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: String, timestamp: Long)

    @Query("UPDATE multi_drug_sessions SET sessionName = :name, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateSessionName(sessionId: String, name: String, timestamp: Long)

    @Query("UPDATE multi_drug_sessions SET patientInfo = :patientInfo, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updatePatientInfo(sessionId: String, patientInfo: String?, timestamp: Long)

    @Query("UPDATE multi_drug_sessions SET scanningPhase = :scanningPhase, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateScanningPhase(sessionId: String, scanningPhase: String, timestamp: Long)

    @Query("UPDATE multi_drug_sessions SET verificationPhase = :verificationPhase, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateVerificationPhase(sessionId: String, verificationPhase: String, timestamp: Long)

    @Query("UPDATE multi_drug_sessions SET batchProcessingPhase = :batchProcessingPhase, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateBatchProcessingPhase(sessionId: String, batchProcessingPhase: String, timestamp: Long)

    @Query("UPDATE multi_drug_sessions SET sessionSummary = :sessionSummary, completedAt = :completedAt, lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateSessionSummary(sessionId: String, sessionSummary: String, completedAt: Long, timestamp: Long)

    // Batch Operations

    @Query("SELECT sessionId FROM multi_drug_sessions WHERE status = :status")
    suspend fun getSessionIdsByStatus(status: String): List<String>

    @Query("UPDATE multi_drug_sessions SET status = :newStatus, lastUpdated = :timestamp WHERE status = :oldStatus")
    suspend fun bulkUpdateStatus(oldStatus: String, newStatus: String, timestamp: Long): Int

    // Advanced Analytics Queries

    @Query("""
        SELECT status, COUNT(*) as count 
        FROM multi_drug_sessions 
        GROUP BY status
    """)
    suspend fun getSessionStatusDistribution(): List<StatusCount>

    @Query("""
        SELECT 
            DATE(createdAt/1000, 'unixepoch') as date,
            COUNT(*) as count
        FROM multi_drug_sessions 
        WHERE createdAt >= :startTime
        GROUP BY DATE(createdAt/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getSessionCountByDate(startTime: Long): List<DateCount>

    @Query("""
        SELECT 
            CASE 
                WHEN (completedAt - createdAt) < 300000 THEN 'Under 5 min'
                WHEN (completedAt - createdAt) < 900000 THEN '5-15 min'
                WHEN (completedAt - createdAt) < 1800000 THEN '15-30 min'
                WHEN (completedAt - createdAt) < 3600000 THEN '30-60 min'
                ELSE 'Over 1 hour'
            END as duration_range,
            COUNT(*) as count
        FROM multi_drug_sessions 
        WHERE status = 'COMPLETED' AND completedAt IS NOT NULL
        GROUP BY duration_range
    """)
    suspend fun getSessionDurationDistribution(): List<DurationCount>

    // Export and Backup Operations

    @Query("SELECT * FROM multi_drug_sessions WHERE lastUpdated >= :timestamp ORDER BY lastUpdated ASC")
    suspend fun getSessionsForBackup(timestamp: Long): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentCompletedSessions(limit: Int): List<MultiDrugSessionEntity>

    @Query("SELECT * FROM multi_drug_sessions WHERE status = 'ERROR' ORDER BY lastUpdated DESC LIMIT :limit")
    suspend fun getRecentFailedSessions(limit: Int): List<MultiDrugSessionEntity>

    // Performance Monitoring

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE lastUpdated >= :timestamp")
    suspend fun getRecentActivityCount(timestamp: Long): Int

    @Query("""
        SELECT 
            AVG(CASE WHEN completedAt IS NOT NULL THEN completedAt - createdAt ELSE NULL END) as avgDuration,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completedCount,
            COUNT(CASE WHEN status = 'ERROR' THEN 1 END) as errorCount,
            COUNT(*) as totalCount
        FROM multi_drug_sessions 
        WHERE createdAt >= :startTime
    """)
    suspend fun getPerformanceMetrics(startTime: Long): PerformanceMetrics?

    // Reactive Queries for Real-time Updates

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE status != 'COMPLETED'")
    fun getActiveSessionCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE status = 'ERROR'")
    fun getErrorSessionCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM multi_drug_sessions WHERE lastUpdated >= :timestamp")
    fun getRecentActivityFlow(timestamp: Long): Flow<Int>

    @Query("""
        SELECT sessionId, sessionName, status, lastUpdated 
        FROM multi_drug_sessions 
        WHERE status != 'COMPLETED' 
        ORDER BY lastUpdated DESC 
        LIMIT 10
    """)
    fun getRecentActiveSessionsFlow(): Flow<List<SessionSummaryInfo>>
}

// Data classes for query results

data class SessionTimeRange(
    val minTime: Long,
    val maxTime: Long,
    val totalCount: Int
)

data class StatusCount(
    val status: String,
    val count: Int
)

data class DateCount(
    val date: String,
    val count: Int
)

data class DurationCount(
    val durationRange: String,
    val count: Int
)

data class PerformanceMetrics(
    val avgDuration: Long?,
    val completedCount: Int,
    val errorCount: Int,
    val totalCount: Int
)

data class SessionSummaryInfo(
    val sessionId: String,
    val sessionName: String,
    val status: String,
    val lastUpdated: Long
)

// Extension functions for common operations

suspend fun MultiDrugSessionDao.getSessionsInLastHours(hours: Int): List<MultiDrugSessionEntity> {
    val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000L)
    return getSessionsSince(cutoffTime)
}

suspend fun MultiDrugSessionDao.getSessionsInLastDays(days: Int): List<MultiDrugSessionEntity> {
    val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
    return getSessionsSince(cutoffTime)
}

suspend fun MultiDrugSessionDao.cleanupOldSessions(daysToKeep: Int): Int {
    val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
    val deletedCompleted = deleteOldCompletedSessions(cutoffTime)
    val deletedFailed = deleteOldFailedSessions(cutoffTime)
    return deletedCompleted + deletedFailed
}

suspend fun MultiDrugSessionDao.markStaleSessionsAsError(): Int {
    val cutoffTime = System.currentTimeMillis() - (2 * 60 * 60 * 1000L) // 2 hours
    return markStaleSessionsAsError(cutoffTime)
}

suspend fun MultiDrugSessionDao.getSessionHealthMetrics(): SessionHealthMetrics {
    val total = getTotalSessionCount()
    val active = getActiveSessionCount()
    val completed = getCompletedSessionCount()
    val errors = getSessionCountByStatus("ERROR")
    val avgDuration = getAverageSessionDuration() ?: 0L
    
    return SessionHealthMetrics(
        totalSessions = total,
        activeSessions = active,
        completedSessions = completed,
        errorSessions = errors,
        averageDuration = avgDuration,
        successRate = if (total > 0) completed.toFloat() / total else 0f,
        errorRate = if (total > 0) errors.toFloat() / total else 0f
    )
}

data class SessionHealthMetrics(
    val totalSessions: Int,
    val activeSessions: Int,
    val completedSessions: Int,
    val errorSessions: Int,
    val averageDuration: Long,
    val successRate: Float,
    val errorRate: Float
)
