package com.zaryif.minimalos.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.zaryif.minimalos.data.repository.NotificationHistoryRepository
import com.zaryif.minimalos.domain.model.NotificationItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationExporter {

    fun exportToTextFile(context: Context, items: List<NotificationItem>, forAssistant: Boolean = false): File {
        val repo = NotificationHistoryRepository.getInstance(context)
        val rawLog = repo.exportFormattedText(items)

        val fullText = if (forAssistant) {
            """
            Please analyze and summarize my day-wise notification log from Minimal OS:
            - Group key insights day by day (Day of week, Date, and Time)
            - Highlight any urgent tasks, important messages, or missed calls that require action
            - Group related alerts by app or topic
            - Provide a concise executive overview

            --- NOTIFICATION ARCHIVE LOG ---
            $rawLog
            """.trimIndent()
        } else {
            rawLog
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = if (forAssistant) "minimal_assistant_prompt_$timestamp.txt" else "minimal_notifications_$timestamp.txt"
        val exportFile = File(context.cacheDir, fileName)
        exportFile.writeText(fullText)
        return exportFile
    }

    fun shareExport(
        context: Context,
        items: List<NotificationItem>,
        forAssistant: Boolean = false,
        assistantPackage: String? = null
    ) {
        if (items.isEmpty()) {
            Toast.makeText(context, "No alerts to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val file = exportToTextFile(context, items, forAssistant)
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val textSnippet = if (forAssistant) {
                "Summarize my recent notifications from Minimal OS:\n\n${file.readText()}"
            } else {
                file.readText()
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, textSnippet)
                putExtra(Intent.EXTRA_SUBJECT, if (forAssistant) "AI Summary of Notifications" else "Minimal OS Notification Log")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // If assistant package specified, try targeting it, otherwise open chooser
            if (forAssistant && !assistantPackage.isNullOrBlank() && assistantPackage != "SYSTEM_DEFAULT") {
                val targetIntent = Intent(shareIntent).apply {
                    `package` = assistantPackage
                }
                if (targetIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(targetIntent)
                    return
                }
            }

            val chooser = Intent.createChooser(
                shareIntent,
                if (forAssistant) "Send to AI Assistant" else "Export Notification Log"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, items: List<NotificationItem>, forAssistant: Boolean = false) {
        if (items.isEmpty()) {
            Toast.makeText(context, "No alerts to copy", Toast.LENGTH_SHORT).show()
            return
        }
        val file = exportToTextFile(context, items, forAssistant)
        val text = file.readText()
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText("Minimal Notifications", text))
        Toast.makeText(context, "Copied log to clipboard", Toast.LENGTH_SHORT).show()
    }

    /**
     * Sends plain text or a note directly to the configured AI Assistant (or shares via intent)
     * with an intelligent summarizing prompt prepended.
     */
    fun shareTextWithAssistant(
        context: Context,
        text: String,
        promptPrefix: String = "Please summarize, organize, and extract key action items from this note:\n\n",
        assistantPackage: String? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            Toast.makeText(context, "Note is empty", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val fullPrompt = "$promptPrefix$trimmed"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, fullPrompt)
                putExtra(Intent.EXTRA_SUBJECT, "AI Summary")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (!assistantPackage.isNullOrBlank() && assistantPackage != "SYSTEM_DEFAULT") {
                val targetIntent = Intent(shareIntent).apply {
                    `package` = assistantPackage
                }
                if (targetIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(targetIntent)
                    return
                }
            }

            val chooser = Intent.createChooser(shareIntent, "Send to AI Assistant").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Directly downloads the notification history as a .txt file to the device's public Downloads directory.
     * Uses MediaStore.Downloads on Android 10+ (Q+) without requiring legacy storage permissions,
     * and falls back to Environment.DIRECTORY_DOWNLOADS on older Android versions.
     */
    fun downloadToDevice(context: Context, items: List<NotificationItem>, forAssistant: Boolean = false): Boolean {
        if (items.isEmpty()) {
            Toast.makeText(context, "No alerts to download", Toast.LENGTH_SHORT).show()
            return false
        }
        return try {
            val file = exportToTextFile(context, items, forAssistant)
            val content = file.readText()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = if (forAssistant) "minimal_assistant_prompt_$timestamp.txt" else "minimal_notifications_$timestamp.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                    Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                    true
                } else {
                    Toast.makeText(context, "Failed to create download file", Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val targetFile = File(downloadsDir, fileName)
                targetFile.writeText(content, Charsets.UTF_8)
                Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}

