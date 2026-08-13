package com.waterproofing.inventory.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.waterproofing.inventory.MainActivity
import com.waterproofing.inventory.R
import com.waterproofing.inventory.data.database.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Background worker that fires once per day and sends a local notification
 * if any non-depleted batches are expiring within the warning window (30 days).
 *
 * Deduplication: we use a single NOTIFICATION_ID, so each run updates the
 * same notification slot. The user is never spammed with repeated alerts.
 */
class ExpiryNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "inventory_expiry_alerts"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "expiry_check_daily"

        /** Alert when a batch expires within this many days. */
        private const val WARNING_DAYS = 30L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Expiry Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for inventory batches approaching expiry"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }
    }

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val warningMs = TimeUnit.DAYS.toMillis(WARNING_DAYS)
        val threshold = now + warningMs

        val db = AppDatabase.getDatabase(context)
        val batches = db.batchDao().getExpiringSoonBatchesOnce(now, threshold)
            .filter { !it.isDepleted && it.currentQuantity > 0 }

        val notificationManager = NotificationManagerCompat.from(context)

        if (batches.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_ID)
            return Result.success()
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val firstBatch = batches.first()
        val daysLeft = ((firstBatch.expiryDate - now) / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(0)

        val title = "Inventory Expiry Alert"
        val bodyText = if (batches.size == 1) {
            "${firstBatch.productName} — ${firstBatch.variantName}\n" +
            "Expires ${sdf.format(Date(firstBatch.expiryDate))} ($daysLeft days left)\n" +
            "Stock: ${firstBatch.currentQuantity} ${firstBatch.unit}"
        } else {
            "${batches.size} batches expiring within $WARNING_DAYS days.\n" +
            "Earliest: ${firstBatch.productName} — ${firstBatch.variantName} " +
            "(${sdf.format(Date(firstBatch.expiryDate))}, $daysLeft days)"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — silently skip
        }

        return Result.success()
    }
}
