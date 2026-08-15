package com.waterproofing.inventory.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.waterproofing.inventory.data.database.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Background worker that runs daily to purge stock transactions older than 6 months.
 *
 * Uses calendar-based 6-month calculation (e.g. 14 Aug -> 14 Feb).
 * Deleting old transactions DOES NOT alter current batch stock quantities.
 */
class TransactionCleanupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "transaction_6month_cleanup"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TransactionCleanupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        suspend fun performCleanup(context: Context) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -6)
            val cutoffMillis = calendar.timeInMillis

            val db = AppDatabase.getDatabase(context)
            db.stockTransactionDao().deleteTransactionsOlderThan(cutoffMillis)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            performCleanup(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
