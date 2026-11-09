package com.zaryif.minimalos.domain.quicknote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.zaryif.minimalos.MainActivity

object QuickNoteManager {
    const val CHANNEL_ID = "minimal_quick_note_channel"
    const val NOTIFICATION_ID = 4040
    const val EXTRA_OPEN_QUICK_NOTE = "OPEN_QUICK_NOTE"

    fun postNotification(context: Context, text: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            cancelNotification(context)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quick Note",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Pinned Quick Note in status bar"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap notification to open Minimal OS straight into Quick Note
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_QUICK_NOTE, true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            1001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Clear button action
        val clearIntent = Intent(context, QuickNoteActionReceiver::class.java).apply {
            action = QuickNoteActionReceiver.ACTION_CLEAR_QUICK_NOTE
        }
        val clearPendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            clearIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val firstLine = trimmed.lineSequence().firstOrNull()?.trim() ?: trimmed

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Quick Note")
            .setContentText(firstLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(trimmed))
            .setOngoing(true) // Pinned to the notification bar
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Clear", clearPendingIntent)
            .setAutoCancel(false)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission not granted yet
        } catch (_: Exception) {}
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }
}

