package com.zaryif.minimalos.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
            Please analyze and summarize my recent notification log from Minimal OS:
            - Highlight any urgent tasks, messages, or missed calls that require action
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
}

