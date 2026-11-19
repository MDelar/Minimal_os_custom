package com.zaryif.minimalos.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.ui.graphics.toArgb
import com.zaryif.minimalos.ui.theme.MinimalThemeRegistry
import java.io.File
import kotlin.math.max

/**
 * Keeps Android's home-screen wallpaper matching what the launcher draws.
 *
 * The launcher paints its own background (theme colour, preset gradient or a
 * photo with a dark scrim) and never touched the system wallpaper, so the
 * system's app-switch animations briefly revealed the stock wallpaper. This
 * renders the same background as a bitmap and sets it as the home wallpaper.
 * The lock-screen wallpaper is left alone.
 *
 * Applied only when the theme or background actually changes, so it doesn't
 * re-set the wallpaper on every launch.
 */
object SystemWallpaperSync {

    private const val PREFS = "system_wallpaper_sync"
    private const val KEY_SIGNATURE = "applied_signature"

    // Must match the preset gradients drawn in HomeScreen.
    private val PRESET_GRADIENTS = mapOf(
        "DEFAULT_MIDNIGHT" to intArrayOf(0xFF04060E.toInt(), 0xFF0B1222.toInt(), 0xFF04060E.toInt()),
        "DEFAULT_SPACE_GRAY" to intArrayOf(0xFF101012.toInt(), 0xFF1C1C1E.toInt(), 0xFF0E0E10.toInt()),
        "DEFAULT_TITANIUM" to intArrayOf(0xFF090611.toInt(), 0xFF171026.toInt(), 0xFF07050E.toInt()),
        "DEFAULT_AMOLED" to intArrayOf(0xFF000000.toInt(), 0xFF000000.toInt(), 0xFF000000.toInt())
    )

    // Same scrim HomeScreen lays over a custom photo (black at 72%).
    private const val PHOTO_SCRIM = 0xB8000000.toInt()

    /** Call off the main thread. Safe to call often: it no-ops when nothing changed. */
    fun syncIfNeeded(context: Context, themeId: String, wallpaperUri: String?) {
        val app = context.applicationContext
        val (width, height) = screenSize(app)
        val signature = signatureFor(themeId, wallpaperUri, width, height)
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_SIGNATURE, null) == signature) return

        val bitmap = try {
            render(app, themeId, wallpaperUri, width, height)
        } catch (_: Throwable) {
            null
        } ?: return

        try {
            val manager = WallpaperManager.getInstance(app)
            if (!manager.isWallpaperSupported || !manager.isSetWallpaperAllowed) return
            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            prefs.edit().putString(KEY_SIGNATURE, signature).apply()
        } catch (_: Exception) {
            // Not fatal: the launcher still draws its own background.
        } finally {
            bitmap.recycle()
        }
    }

    private fun signatureFor(themeId: String, uri: String?, width: Int, height: Int): String {
        val fileStamp = uri?.let { File(it) }?.takeIf { it.exists() }?.let { "${it.lastModified()}:${it.length()}" } ?: ""
        return listOf(themeId, uri ?: "", fileStamp, "${width}x$height").joinToString("|")
    }

    private fun render(context: Context, themeId: String, uri: String?, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(MinimalThemeRegistry.getTheme(themeId).background.toArgb())

        if (uri.isNullOrBlank()) return bitmap

        val gradient = PRESET_GRADIENTS[uri]
        if (gradient != null) {
            val paint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height.toFloat(), gradient, null, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            return bitmap
        }

        val photo = decodeSampled(context, uri, max(width, height)) ?: return bitmap
        // Centre-crop, like ContentScale.Crop on the home screen.
        val scale = max(width.toFloat() / photo.width, height.toFloat() / photo.height)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate((width - photo.width * scale) / 2f, (height - photo.height * scale) / 2f)
        }
        canvas.drawBitmap(photo, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        photo.recycle()
        canvas.drawColor(PHOTO_SCRIM)
        return bitmap
    }

    private fun decodeSampled(context: Context, path: String, maxDim: Int): Bitmap? {
        val file = File(path)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath, bounds)
        } else {
            context.contentResolver.openInputStream(Uri.parse(path))?.use { BitmapFactory.decodeStream(it, null, bounds) }
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxDim && bounds.outHeight / (sample * 2) >= maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } else {
            context.contentResolver.openInputStream(Uri.parse(path))?.use { BitmapFactory.decodeStream(it, null, opts) }
        }
    }

    private fun screenSize(context: Context): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds: Rect = wm.maximumWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            metrics.widthPixels to metrics.heightPixels
        }
    }
}
