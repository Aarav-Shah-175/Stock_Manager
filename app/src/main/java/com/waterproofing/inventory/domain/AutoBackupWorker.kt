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
import com.waterproofing.inventory.data.database.AppDatabase
import com.waterproofing.inventory.data.repository.SettingsRepository
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "inventory_backup_notifications"
        const val NOTIFICATION_SUCCESS_ID = 2001
        const val NOTIFICATION_FAILURE_ID = 2002
        const val WORK_NAME = "automatic_daily_backup"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(
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
                    "Backup Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for automatic inventory database backups"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        }
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(context)
        val settingsRepo = SettingsRepository(db.appSettingsDao())

        val isEnabled = settingsRepo.getAutoBackupEnabled()
        if (!isEnabled) {
            return Result.success()
        }

        val keepCount = settingsRepo.getAutoBackupKeepCount()
        val customFolderUri = settingsRepo.getAutoBackupFolderUri()
        val result = BackupManager.performAutoBackup(context, keepCount, customFolderUri)

        val notificationManager = NotificationManagerCompat.from(context)

        return result.fold(
            onSuccess = { file ->
                val now = System.currentTimeMillis()
                settingsRepo.setLastSuccessfulBackup(now)
                settingsRepo.setLastBackupStatus("SUCCESS")

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_save)
                    .setContentTitle("Inventory Backup Complete")
                    .setContentText("Your daily inventory backup was completed successfully.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(NOTIFICATION_SUCCESS_ID, notification)
                } catch (_: SecurityException) {}

                Result.success()
            },
            onFailure = { e ->
                settingsRepo.setLastBackupStatus("FAILED: ${e.message}")

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Inventory Backup Failed")
                    .setContentText("Your automatic inventory backup could not be completed. Open app to check settings.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(NOTIFICATION_FAILURE_ID, notification)
                } catch (_: SecurityException) {}

                Result.failure()
            }
        )
    }
}
