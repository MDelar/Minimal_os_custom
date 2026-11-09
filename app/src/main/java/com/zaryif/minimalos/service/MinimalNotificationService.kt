package com.zaryif.minimalos.service

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.data.repository.NotificationHistoryRepository
import com.zaryif.minimalos.domain.media.MinimalMediaManager
import com.zaryif.minimalos.domain.model.NotificationCategory
import com.zaryif.minimalos.domain.model.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MinimalNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this
        userPreferences = UserPreferences(this)
        MinimalMediaManager.init(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        MinimalMediaManager.cleanup()
        if (instance == this) instance = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        refreshActiveNotifications()
        MinimalMediaManager.init(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            addNotification(it)
            MinimalMediaManager.refresh(this)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            removeNotification(it.key)
            MinimalMediaManager.refresh(this)
        }
    }

    private fun addNotification(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val pkg = sbn.packageName

        if (title.isBlank() && text.isBlank()) return
        if (pkg == packageName) return

        val category = categorize(pkg, sbn.notification.category)

        val item = NotificationItem(
            id = sbn.id,
            key = sbn.key,
            appName = getAppName(pkg),
            title = title,
            content = text,
            timestamp = sbn.postTime,
            packageName = pkg,
            category = category,
            pendingIntent = sbn.notification.contentIntent
        )

        val current = _notifications.value.filterNot { it.key == sbn.key }
        _notifications.value = listOf(item) + current

        // Persist notification locally so history is never lost (if enabled by user)
        serviceScope.launch {
            if (userPreferences.notificationHistoryEnabled.first()) {
                NotificationHistoryRepository.getInstance(this@MinimalNotificationService).saveNotification(item)
            }
        }
    }

    private fun removeNotification(key: String) {
        _notifications.value = _notifications.value.filterNot { it.key == key }
    }

    fun dismissNotification(key: String) {
        try {
            cancelNotification(key)
        } catch (_: Exception) {}
        removeNotification(key)
    }

    private fun refreshActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            val list = mutableListOf<NotificationItem>()
            val historyRepo = NotificationHistoryRepository.getInstance(this)
            for (sbn in active) {
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val pkg = sbn.packageName
                if (title.isBlank() && text.isBlank()) continue
                if (pkg == packageName) continue

                val category = categorize(pkg, sbn.notification.category)
                val item = NotificationItem(
                    id = sbn.id,
                    key = sbn.key,
                    appName = getAppName(pkg),
                    title = title,
                    content = text,
                    timestamp = sbn.postTime,
                    packageName = pkg,
                    category = category,
                    pendingIntent = sbn.notification.contentIntent
                )
                list.add(item)
            }
            _notifications.value = list.sortedByDescending { it.timestamp }

            serviceScope.launch {
                if (userPreferences.notificationHistoryEnabled.first()) {
                    historyRepo.saveNotifications(list)
                }
            }
        } catch (_: Exception) {}
    }

    private fun categorize(pkg: String, androidCategory: String?): NotificationCategory {
        val lowerPkg = pkg.lowercase()
        return when {
            androidCategory == Notification.CATEGORY_MESSAGE ||
            lowerPkg.contains("sms") || lowerPkg.contains("mms") ||
            lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
            lowerPkg.contains("signal") || lowerPkg.contains("messenger") -> NotificationCategory.MESSAGE

            androidCategory == Notification.CATEGORY_CALL ||
            androidCategory == Notification.CATEGORY_MISSED_CALL ||
            lowerPkg.contains("dialer") || lowerPkg.contains("phone") ||
            lowerPkg.contains("telecom") -> NotificationCategory.CALLS

            androidCategory == Notification.CATEGORY_EMAIL ||
            lowerPkg.contains("gmail") || lowerPkg.contains("mail") ||
            lowerPkg.contains("outlook") -> NotificationCategory.EMAIL

            else -> NotificationCategory.OTHER
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.')
        }
    }

    companion object {
        var instance: MinimalNotificationService? = null
            private set

        private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

        fun isPermissionGranted(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }

        fun toggleFlag(key: String) {
            _notifications.value = _notifications.value.map {
                if (it.key == key) it.copy(isFlagged = !it.isFlagged) else it
            }
        }

        fun dismiss(key: String) {
            instance?.dismissNotification(key) ?: run {
                _notifications.value = _notifications.value.filterNot { it.key == key }
            }
        }

        fun dismissAll() {
            instance?.let { service ->
                try {
                    service.cancelAllNotifications()
                } catch (_: Exception) {}
            }
            _notifications.value = emptyList()
        }
    }
}
