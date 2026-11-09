package com.zaryif.minimalos.domain.quicknote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zaryif.minimalos.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuickNoteActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CLEAR_QUICK_NOTE = "com.zaryif.minimalos.ACTION_CLEAR_QUICK_NOTE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val userPreferences = UserPreferences(appContext)

        when (intent.action) {
            ACTION_CLEAR_QUICK_NOTE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    userPreferences.setQuickNote("")
                    QuickNoteManager.cancelNotification(appContext)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val savedNote = userPreferences.quickNote.first()
                    val isPinned = userPreferences.quickNotePinned.first()
                    if (savedNote.isNotBlank() && isPinned) {
                        QuickNoteManager.postNotification(appContext, savedNote)
                    }
                }
            }
        }
    }
}

