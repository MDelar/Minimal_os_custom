package com.zaryif.minimalos.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.zaryif.minimalos.domain.model.NotificationCategory
import com.zaryif.minimalos.domain.model.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationHistoryRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _history = MutableStateFlow<List<NotificationItem>>(emptyList())
    val history: StateFlow<List<NotificationItem>> = _history.asStateFlow()

    init {
        reloadHistory()
    }

    fun reloadHistory() {
        scope.launch {
            _history.value = queryNotifications(category = NotificationCategory.ALL, query = "", limit = 300)
        }
    }

    fun saveNotification(item: NotificationItem) {
        if (item.title.isBlank() && item.content.isBlank()) return

        scope.launch {
            try {
                val safeKey = item.key.ifBlank { "${item.packageName}_${item.id}_${item.timestamp}" }
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(COLUMN_KEY, safeKey)
                    put(COLUMN_PACKAGE, item.packageName)
                    put(COLUMN_APP_NAME, item.appName)
                    put(COLUMN_TITLE, item.title)
                    put(COLUMN_CONTENT, item.content)
                    put(COLUMN_CATEGORY, item.category.name)
                    put(COLUMN_TIMESTAMP, item.timestamp)
                    put(COLUMN_FLAGGED, if (item.isFlagged) 1 else 0)
                }

                db.insertWithOnConflict(
                    TABLE_NOTIFICATIONS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )

                // Maintain storage limit to prevent infinite growth (keep last 1000)
                db.execSQL(
                    """
                    DELETE FROM $TABLE_NOTIFICATIONS 
                    WHERE $COLUMN_ID NOT IN (
                        SELECT $COLUMN_ID FROM $TABLE_NOTIFICATIONS 
                        ORDER BY $COLUMN_TIMESTAMP DESC 
                        LIMIT 1000
                    )
                    """.trimIndent()
                )

                _history.value = queryNotifications(category = NotificationCategory.ALL, query = "", limit = 300)
            } catch (_: Exception) {}
        }
    }

    fun saveNotifications(items: List<NotificationItem>) {
        if (items.isEmpty()) return
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    for (item in items) {
                        if (item.title.isBlank() && item.content.isBlank()) continue
                        val safeKey = item.key.ifBlank { "${item.packageName}_${item.id}_${item.timestamp}" }
                        val values = ContentValues().apply {
                            put(COLUMN_KEY, safeKey)
                            put(COLUMN_PACKAGE, item.packageName)
                            put(COLUMN_APP_NAME, item.appName)
                            put(COLUMN_TITLE, item.title)
                            put(COLUMN_CONTENT, item.content)
                            put(COLUMN_CATEGORY, item.category.name)
                            put(COLUMN_TIMESTAMP, item.timestamp)
                            put(COLUMN_FLAGGED, if (item.isFlagged) 1 else 0)
                        }
                        db.insertWithOnConflict(
                            TABLE_NOTIFICATIONS,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                    }
                    db.execSQL(
                        """
                        DELETE FROM $TABLE_NOTIFICATIONS 
                        WHERE $COLUMN_ID NOT IN (
                            SELECT $COLUMN_ID FROM $TABLE_NOTIFICATIONS 
                            ORDER BY $COLUMN_TIMESTAMP DESC 
                            LIMIT 1000
                        )
                        """.trimIndent()
                    )
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                _history.value = queryNotifications(category = NotificationCategory.ALL, query = "", limit = 300)
            } catch (_: Exception) {}
        }
    }

    fun deleteNotification(key: String) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                db.delete(TABLE_NOTIFICATIONS, "$COLUMN_KEY = ?", arrayOf(key))
                _history.value = _history.value.filterNot { it.key == key }
            } catch (_: Exception) {}
        }
    }

    fun toggleFlag(key: String) {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                val current = _history.value.find { it.key == key } ?: return@launch
                val newFlag = if (current.isFlagged) 0 else 1
                val values = ContentValues().apply {
                    put(COLUMN_FLAGGED, newFlag)
                }
                db.update(TABLE_NOTIFICATIONS, values, "$COLUMN_KEY = ?", arrayOf(key))
                _history.value = _history.value.map {
                    if (it.key == key) it.copy(isFlagged = newFlag == 1) else it
                }
            } catch (_: Exception) {}
        }
    }

    fun clearHistory() {
        scope.launch {
            try {
                val db = dbHelper.writableDatabase
                db.delete(TABLE_NOTIFICATIONS, null, null)
                _history.value = emptyList()
            } catch (_: Exception) {}
        }
    }

    fun queryNotifications(
        category: NotificationCategory = NotificationCategory.ALL,
        query: String = "",
        limit: Int = 200
    ): List<NotificationItem> {
        val list = mutableListOf<NotificationItem>()
        try {
            val db = dbHelper.readableDatabase

            val selectionParts = mutableListOf<String>()
            val args = mutableListOf<String>()

            if (category != NotificationCategory.ALL) {
                selectionParts.add("$COLUMN_CATEGORY = ?")
                args.add(category.name)
            }

            if (query.isNotBlank()) {
                val clean = "%${query.trim()}%"
                selectionParts.add("($COLUMN_TITLE LIKE ? OR $COLUMN_CONTENT LIKE ? OR $COLUMN_APP_NAME LIKE ?)")
                args.add(clean)
                args.add(clean)
                args.add(clean)
            }

            val selection = if (selectionParts.isNotEmpty()) selectionParts.joinToString(" AND ") else null
            val selectionArgs = if (args.isNotEmpty()) args.toTypedArray() else null

            val cursor: Cursor = db.query(
                TABLE_NOTIFICATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "$COLUMN_TIMESTAMP DESC",
                limit.toString()
            )

            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val key = it.getString(it.getColumnIndexOrThrow(COLUMN_KEY)) ?: ""
                    val pkg = it.getString(it.getColumnIndexOrThrow(COLUMN_PACKAGE)) ?: ""
                    val appName = it.getString(it.getColumnIndexOrThrow(COLUMN_APP_NAME)) ?: ""
                    val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)) ?: ""
                    val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT)) ?: ""
                    val catName = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)) ?: "OTHER"
                    val timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    val flagged = it.getInt(it.getColumnIndexOrThrow(COLUMN_FLAGGED)) == 1

                    val cat = try {
                        NotificationCategory.valueOf(catName)
                    } catch (_: Exception) {
                        NotificationCategory.OTHER
                    }

                    list.add(
                        NotificationItem(
                            id = id,
                            key = key,
                            appName = appName,
                            title = title,
                            content = content,
                            timestamp = timestamp,
                            packageName = pkg,
                            category = cat,
                            isFlagged = flagged,
                            pendingIntent = null
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return list
    }

    fun exportFormattedText(items: List<NotificationItem>): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val exportTimestamp = SimpleDateFormat("EEEE, yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("MINIMAL OS · NOTIFICATION ARCHIVE LOG")
        sb.appendLine("Exported: $exportTimestamp")
        sb.appendLine("Total Entries: ${items.size}")
        sb.appendLine("==================================================")
        sb.appendLine()

        if (items.isEmpty()) {
            sb.appendLine("(No notification history recorded)")
            return sb.toString()
        }

        // Group notifications day-wise (maintaining chronological order)
        val groupedByDay = items.groupBy { dayKeyFormat.format(Date(it.timestamp)) }

        var entryCounter = 1
        groupedByDay.forEach { (_, dayItems) ->
            val firstDate = Date(dayItems.first().timestamp)
            val dayHeader = dayFormat.format(firstDate).uppercase()

            sb.appendLine("══════════════════════════════════════════════════")
            sb.appendLine("📅 DAY: $dayHeader (${dayItems.size} alerts)")
            sb.appendLine("══════════════════════════════════════════════════")
            sb.appendLine()

            dayItems.forEach { item ->
                val timeStr = timeFormat.format(Date(item.timestamp))
                val dateStr = dayKeyFormat.format(Date(item.timestamp))
                val appLabel = item.appName.ifBlank { item.packageName }
                sb.appendLine("[$entryCounter] TIME: $timeStr · DATE: $dateStr · $appLabel (${item.category.name})")
                if (item.title.isNotBlank()) {
                    sb.appendLine("Title: ${item.title.trim()}")
                }
                if (item.content.isNotBlank()) {
                    sb.appendLine("Message: ${item.content.trim()}")
                }
                sb.appendLine("--------------------------------------------------")
                entryCounter++
            }
            sb.appendLine()
        }

        return sb.toString()
    }

    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE $TABLE_NOTIFICATIONS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_KEY TEXT UNIQUE,
                    $COLUMN_PACKAGE TEXT,
                    $COLUMN_APP_NAME TEXT,
                    $COLUMN_TITLE TEXT,
                    $COLUMN_CONTENT TEXT,
                    $COLUMN_CATEGORY TEXT,
                    $COLUMN_TIMESTAMP INTEGER,
                    $COLUMN_FLAGGED INTEGER DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX idx_notifications_timestamp ON $TABLE_NOTIFICATIONS ($COLUMN_TIMESTAMP DESC)")
            db.execSQL("CREATE INDEX idx_notifications_pkg ON $TABLE_NOTIFICATIONS ($COLUMN_PACKAGE)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
            onCreate(db)
        }
    }

    companion object {
        private const val DATABASE_NAME = "minimal_notifications.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NOTIFICATIONS = "notification_history"
        private const val COLUMN_ID = "id"
        private const val COLUMN_KEY = "sbn_key"
        private const val COLUMN_PACKAGE = "package_name"
        private const val COLUMN_APP_NAME = "app_name"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_FLAGGED = "is_flagged"

        @Volatile
        private var instance: NotificationHistoryRepository? = null

        fun getInstance(context: Context): NotificationHistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: NotificationHistoryRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

