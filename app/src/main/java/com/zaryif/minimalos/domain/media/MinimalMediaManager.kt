package com.zaryif.minimalos.domain.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.zaryif.minimalos.service.MinimalNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaTrackInfo(
    val title: String,
    val artist: String,
    val album: String = "",
    val packageName: String,
    val appName: String,
    val isPlaying: Boolean
)

object MinimalMediaManager {

    private val _currentTrack = MutableStateFlow<MediaTrackInfo?>(null)
    val currentTrack: StateFlow<MediaTrackInfo?> = _currentTrack.asStateFlow()

    private var activeController: MediaController? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var sessionListener: MediaSessionManager.OnActiveSessionsChangedListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            mainHandler.post {
                updateTrackState()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            mainHandler.post {
                updateTrackState()
            }
        }

        override fun onSessionDestroyed() {
            mainHandler.post {
                activeController = null
                _currentTrack.value = null
            }
        }
    }

    fun init(context: Context) {
        if (sessionListener != null) {
            refresh(context)
            return
        }
        try {
            val sm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            mediaSessionManager = sm

            val component = ComponentName(context, MinimalNotificationService::class.java)

            sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                mainHandler.post {
                    bindControllers(context, controllers)
                }
            }

            try {
                sm.addOnActiveSessionsChangedListener(sessionListener!!, component)
                val initialControllers = sm.getActiveSessions(component)
                bindControllers(context, initialControllers)
            } catch (_: SecurityException) {
                // Notification listener permission not granted yet
            } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    fun refresh(context: Context) {
        try {
            val sm = mediaSessionManager ?: (context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager)
            if (sm != null) {
                val component = ComponentName(context, MinimalNotificationService::class.java)
                val controllers = sm.getActiveSessions(component)
                bindControllers(context, controllers)
            }
        } catch (_: Exception) {}
    }

    private fun bindControllers(context: Context, controllers: List<MediaController>?) {
        try {
            if (controllers.isNullOrEmpty()) {
                activeController?.unregisterCallback(controllerCallback)
                activeController = null
                _currentTrack.value = null
                return
            }

            // Prefer playing session over paused session
            val preferredController = controllers.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: controllers.firstOrNull {
                val state = it.playbackState?.state
                state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_BUFFERING
            } ?: controllers.firstOrNull()

            if (preferredController != activeController) {
                activeController?.unregisterCallback(controllerCallback)
                activeController = preferredController
                activeController?.registerCallback(controllerCallback)
            }

            updateTrackState(context)
        } catch (_: Exception) {}
    }

    private fun updateTrackState(context: Context? = null) {
        val controller = activeController
        if (controller == null) {
            _currentTrack.value = null
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val state = playbackState?.state
        val isPlaying = state == PlaybackState.STATE_PLAYING ||
                state == PlaybackState.STATE_BUFFERING ||
                state == PlaybackState.STATE_FAST_FORWARDING ||
                state == PlaybackState.STATE_REWINDING

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: ""

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: ""

        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

        val pkg = controller.packageName ?: ""
        val appName = try {
            if (context != null) {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(info).toString()
            } else {
                pkg.substringAfterLast('.')
            }
        } catch (_: Exception) {
            pkg.substringAfterLast('.')
        }

        // If title and artist are completely empty and state is stopped/none, hide track
        if (title.isBlank() && artist.isBlank() && !isPlaying) {
            _currentTrack.value = null
            return
        }

        _currentTrack.value = MediaTrackInfo(
            title = title.ifBlank { "audio playing" },
            artist = artist.ifBlank { appName },
            album = album,
            packageName = pkg,
            appName = appName,
            isPlaying = isPlaying
        )
    }

    fun togglePlayPause(context: Context) {
        val controller = activeController
        if (controller != null) {
            try {
                val state = controller.playbackState?.state
                val transport = controller.transportControls
                if (state == PlaybackState.STATE_PLAYING) {
                    transport.pause()
                } else {
                    transport.play()
                }
                return
            } catch (_: Exception) {}
        }

        // Hardware media key fallback
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            )
            audioManager?.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            )
        } catch (_: Exception) {}
    }

    fun skipToNext(context: Context) {
        val controller = activeController
        if (controller != null) {
            try {
                controller.transportControls.skipToNext()
                return
            } catch (_: Exception) {}
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
            )
            audioManager?.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT)
            )
        } catch (_: Exception) {}
    }

    fun skipToPrevious(context: Context) {
        val controller = activeController
        if (controller != null) {
            try {
                controller.transportControls.skipToPrevious()
                return
            } catch (_: Exception) {}
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            )
            audioManager?.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            )
        } catch (_: Exception) {}
    }

    fun openPlayer(context: Context) {
        try {
            val pkg = activeController?.packageName
            if (!pkg.isNullOrBlank()) {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }
        } catch (_: Exception) {}
    }

    fun cleanup() {
        try {
            activeController?.unregisterCallback(controllerCallback)
            activeController = null
            sessionListener?.let {
                mediaSessionManager?.removeOnActiveSessionsChangedListener(it)
            }
            sessionListener = null
            _currentTrack.value = null
        } catch (_: Exception) {}
    }
}

