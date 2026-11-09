package com.zaryif.minimalos.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.zaryif.minimalos.domain.model.AppInfo

class AppRepository(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                AppInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = null,
                    isSystemApp = (appInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0) != 0
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun searchApps(query: String, apps: List<AppInfo>): List<AppInfo> {
        if (query.isBlank()) return apps
        val lowerQuery = query.lowercase()
        return apps.filter { app ->
            app.label.lowercase().contains(lowerQuery) ||
                app.packageName.lowercase().contains(lowerQuery)
        }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }
}
