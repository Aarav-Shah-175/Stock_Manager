package com.waterproofing.inventory.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    private const val DB_NAME = "waterproofing_inventory_db"
    private const val BACKUP_PREFIX = "WaterproofInventory_Backup"
    private const val BACKUP_EXT = ".db"

    /**
     * Returns the primary Room DB file. Room uses WAL mode so we must also
     * copy the -shm and -wal sidecar files if they exist.
     */
    private fun dbFile(context: Context): File =
        context.getDatabasePath(DB_NAME)

    /**
     * Copies the current database to the app's external files directory,
     * then returns a FileProvider URI suitable for sharing via an Intent.
     *
     * Caller must call [AppDatabase.close()] and checkpoint WAL BEFORE calling this,
     * or pass a freshly checkpointed database reference.
     */
    fun export(context: Context): Result<Uri> = runCatching {
        val src = dbFile(context)
        check(src.exists()) { "Database file not found." }

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
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
     * The app MUST be restarted after calling this for Room to pick up the new DB.
     * Returns the backup file name on success.
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

        // Extract a friendly display name
        sourceUri.lastPathSegment ?: "backup file"
    }
}
