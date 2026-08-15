package com.waterproofing.inventory.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.waterproofing.inventory.data.database.AppDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    private const val DB_NAME = "waterproofing_inventory_db"
    private const val BACKUP_PREFIX = "Inventory_Backup"
    private const val BACKUP_EXT = ".db"

    /**
     * Returns the primary Room DB file.
     */
    private fun dbFile(context: Context): File =
        context.getDatabasePath(DB_NAME)

    /**
     * Copies the current database to the app's external files directory,
     * then returns a FileProvider URI suitable for sharing via an Intent.
     */
    fun export(context: Context): Result<Uri> = runCatching {
        val src = dbFile(context)
        check(src.exists()) { "Database file not found." }

        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "${BACKUP_PREFIX}_${sdf.format(Date())}${BACKUP_EXT}"

        val destDir = context.getExternalFilesDir(null)
            ?: context.filesDir
        destDir.mkdirs()
        val dest = File(destDir, fileName)

        // Copy main DB file
        src.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Copy WAL and SHM sidecar files if present
        listOf("$DB_NAME-wal", "$DB_NAME-shm").forEach { sidecar ->
            val sidecarFile = File(src.parent, sidecar)
            if (sidecarFile.exists()) {
                val sidecarDest = File(destDir, "${dest.nameWithoutExtension}_${sidecar.substringAfterLast('-')}")
                sidecarFile.copyTo(sidecarDest, overwrite = true)
            }
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            dest
        )
    }

    /**
     * Returns a share Intent for the given backup URI.
     */
    fun buildShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Restores the database from the given content URI (from SAF file picker).
     */
    fun import(context: Context, sourceUri: Uri): Result<String> = runCatching {
        val dest = dbFile(context)
        dest.parentFile?.mkdirs()

        // Close WAL before overwriting
        val walFile = File(dest.parent, "$DB_NAME-wal")
        val shmFile = File(dest.parent, "$DB_NAME-shm")
        walFile.delete()
        shmFile.delete()

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        } ?: error("Could not open source file for reading.")

        sourceUri.lastPathSegment ?: "backup file"
    }

    /**
     * Performs automatic local daily backup safely to app internal storage or custom folder URI.
     * Prunes old backups keeping [keepCount] newest files.
     */
    fun performAutoBackup(
        context: Context,
        keepCount: Int = 7,
        customFolderUriStr: String? = null
    ): Result<String> = runCatching {
        // 1. Checkpoint WAL for DB consistency
        try {
            AppDatabase.getDatabase(context).openHelper.writableDatabase
                .execSQL("PRAGMA wal_checkpoint(FULL)")
        } catch (_: Exception) {}

        val src = dbFile(context)
        check(src.exists()) { "Database file not found." }

        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "${BACKUP_PREFIX}_${sdf.format(Date())}${BACKUP_EXT}"

        // If user specified custom SAF folder URI, try writing to custom folder
        if (!customFolderUriStr.isNullOrBlank()) {
            val customResult = runCatching {
                val treeUri = Uri.parse(customFolderUriStr)
                val targetDir = DocumentFile.fromTreeUri(context, treeUri)
                check(targetDir != null && targetDir.exists() && targetDir.canWrite()) {
                    "Custom backup directory is inaccessible or read-only."
                }

                val docFile = targetDir.createFile("application/octet-stream", fileName)
                    ?: error("Could not create file in custom backup folder.")

                src.inputStream().use { input ->
                    context.contentResolver.openOutputStream(docFile.uri)?.use { output ->
                        input.copyTo(output)
                    } ?: error("Could not open output stream for custom backup file.")
                }

                // Prune old backups in custom folder
                val backups = targetDir.listFiles()
                    .filter { file -> file.name?.startsWith(BACKUP_PREFIX) == true }
                    .sortedByDescending { it.lastModified() }

                val effectiveKeepCount = keepCount.coerceAtLeast(1)
                if (backups.size > effectiveKeepCount) {
                    backups.drop(effectiveKeepCount).forEach { oldBackup ->
                        oldBackup.delete()
                    }
                }

                docFile.name ?: fileName
            }

            if (customResult.isSuccess) {
                return customResult
            }
        }

        // Default: internal app files directory (/files/backups)
        val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
        val tempDest = File(backupDir, "$fileName.tmp")
        val finalDest = File(backupDir, fileName)

        // Write to temp file first
        src.inputStream().use { input ->
            tempDest.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        check(tempDest.exists() && tempDest.length() > 0) { "Backup file creation failed or file is empty." }

        if (finalDest.exists()) finalDest.delete()
        check(tempDest.renameTo(finalDest)) { "Failed to finalize backup file." }

        // Safely prune older backups exceeding keepCount
        val backups = backupDir.listFiles { file ->
            file.name.startsWith(BACKUP_PREFIX) && file.name.endsWith(BACKUP_EXT)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        val effectiveKeepCount = keepCount.coerceAtLeast(1)
        if (backups.size > effectiveKeepCount) {
            backups.drop(effectiveKeepCount).forEach { oldBackup ->
                oldBackup.delete()
            }
        }

        finalDest.name
    }
}
