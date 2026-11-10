package com.zaryif.minimalos

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.data.repository.AppRepository
import com.zaryif.minimalos.data.repository.ContactRepository
import com.zaryif.minimalos.data.repository.StorageRepository
import com.zaryif.minimalos.domain.quicknote.QuickNoteManager
import com.zaryif.minimalos.domain.usecase.ExecuteCommandUseCase
import com.zaryif.minimalos.ui.apps.AppDrawerScreen
import com.zaryif.minimalos.ui.components.MinimalClockSheet
import com.zaryif.minimalos.ui.components.MinimalMemoSheet
import com.zaryif.minimalos.ui.home.HomeScreen
import com.zaryif.minimalos.ui.hub.HubScreen
import com.zaryif.minimalos.ui.onboarding.OnboardingScreen
import com.zaryif.minimalos.ui.settings.SettingsScreen
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalOSTheme
import com.zaryif.minimalos.ui.widgets.WidgetsDeckScreen
import kotlinx.coroutines.launch

enum class ScreenState {
    ONBOARDING,
    HOME,
    APPS,
    HUB,
    WIDGETS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    companion object {
        private const val APPWIDGET_HOST_ID = 2048
    }

    private lateinit var userPreferences: UserPreferences
    private lateinit var appRepository: AppRepository
    private lateinit var storageRepository: StorageRepository
    private lateinit var contactRepository: ContactRepository
    private lateinit var executeCommandUseCase: ExecuteCommandUseCase

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private var isAddingToHome = false

    private val pickWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (appWidgetId != -1) {
                val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                if (appWidgetInfo?.configure != null) {
                    val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                        component = appWidgetInfo.configure
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    configWidgetLauncher.launch(configIntent)
                } else {
                    lifecycleScope.launch {
                        if (isAddingToHome) {
                            userPreferences.addHomeSystemWidget(appWidgetId)
                        } else {
                            userPreferences.addDeckSystemWidget(appWidgetId)
                        }
                    }
                }
            }
        }
    }

    private val configWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (appWidgetId != -1) {
                lifecycleScope.launch {
                    if (isAddingToHome) {
                        userPreferences.addHomeSystemWidget(appWidgetId)
                    } else {
                        userPreferences.addDeckSystemWidget(appWidgetId)
                    }
                }
            }
        }
    }

    fun launchDeckWidgetPicker() {
        isAddingToHome = false
        val newId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
        }
        pickWidgetLauncher.launch(pickIntent)
    }

    fun launchHomeWidgetPicker() {
        isAddingToHome = true
        val newId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
        }
        pickWidgetLauncher.launch(pickIntent)
    }

    fun deleteDeckWidget(id: Int) {
        appWidgetHost.deleteAppWidgetId(id)
        lifecycleScope.launch {
            userPreferences.removeDeckSystemWidget(id)
        }
    }

    fun deleteHomeWidget(id: Int) {
        appWidgetHost.deleteAppWidgetId(id)
        lifecycleScope.launch {
            userPreferences.removeHomeSystemWidget(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)

        userPreferences = UserPreferences(this)
        appRepository = AppRepository(this)
        storageRepository = StorageRepository(this)
        contactRepository = ContactRepository(this)
        executeCommandUseCase = ExecuteCommandUseCase(
            this,
            appRepository,
            storageRepository,
            contactRepository,
            userPreferences
        )

        lifecycleScope.launch {
            val note = userPreferences.quickNote.first()
            val isPinned = userPreferences.quickNotePinned.first()
            if (note.isNotBlank() && isPinned) {
                QuickNoteManager.postNotification(this@MainActivity, note)
            }
        }

        setContent {
            val themeId by userPreferences.themeId.collectAsState(initial = "AMOLED_RED")
            MinimalOSTheme(themeId = themeId) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MinimalBlack
                ) {
                    MinimalAppContent(
                        userPreferences = userPreferences,
                        appRepository = appRepository,
                        storageRepository = storageRepository,
                        contactRepository = contactRepository,
                        executeCommandUseCase = executeCommandUseCase,
                        appWidgetManager = appWidgetManager,
                        appWidgetHost = appWidgetHost,
                        onAddDeckWidget = { launchDeckWidgetPicker() },
                        onDeleteDeckWidget = { deleteDeckWidget(it) },
                        onAddHomeWidget = { launchHomeWidgetPicker() },
                        onDeleteHomeWidget = { deleteHomeWidget(it) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }
}

@Composable
fun MinimalAppContent(
    userPreferences: UserPreferences,
    appRepository: AppRepository,
    storageRepository: StorageRepository,
    contactRepository: ContactRepository,
    executeCommandUseCase: ExecuteCommandUseCase,
    appWidgetManager: AppWidgetManager,
    appWidgetHost: AppWidgetHost,
    onAddDeckWidget: () -> Unit,
    onDeleteDeckWidget: (Int) -> Unit,
    onAddHomeWidget: () -> Unit,
    onDeleteHomeWidget: (Int) -> Unit
) {
    var installedApps by remember { mutableStateOf(appRepository.getInstalledApps()) }
    val favoriteAppPackages by userPreferences.favoriteApps.collectAsState(initial = emptySet())
    val deckWidgetIds by userPreferences.deckSystemWidgets.collectAsState(initial = emptySet())
    val onboardingCompleted by userPreferences.onboardingCompleted.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                installedApps = appRepository.getInstalledApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(installedApps) {
        val installedPackageNames = installedApps.map { it.packageName }.toSet()
        val staleFavs = favoriteAppPackages.filter { it !in installedPackageNames }
        if (staleFavs.isNotEmpty()) {
            userPreferences.setFavoriteApps(favoriteAppPackages - staleFavs.toSet())
        }
    }

    var currentScreen by remember { mutableStateOf<ScreenState?>(null) }

    // First time launch: while DataStore loads initial value, show neutral black to prevent Home flash
    if (onboardingCompleted == null && currentScreen == null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = com.zaryif.minimalos.ui.theme.MinimalBlack
        ) {}
        return
    }

    val activeScreen = currentScreen ?: when (onboardingCompleted) {
        false -> ScreenState.ONBOARDING
        true -> ScreenState.HOME
        null -> ScreenState.HOME
    }

    // Mini Apps bottom sheet states
    var showMemoSheet by remember { mutableStateOf(false) }
    var showClockSheet by remember { mutableStateOf(false) }

    // Android back button handling returns to Home screen (disabled on HOME and during first-time ONBOARDING)
    BackHandler(enabled = activeScreen != ScreenState.HOME && (activeScreen != ScreenState.ONBOARDING || onboardingCompleted == true)) {
        currentScreen = ScreenState.HOME
    }

    AnimatedContent(
        targetState = activeScreen,
        transitionSpec = {
            when {
                // Navigating to/from APPS (App Drawer): slide up from bottom
                targetState == ScreenState.APPS -> {
                    (slideInVertically(spring(dampingRatio = 0.85f, stiffness = 500f)) { it / 4 } + fadeIn(tween(200)))
                        .togetherWith(fadeOut(tween(140)))
                }
                initialState == ScreenState.APPS -> {
                    fadeIn(tween(150))
                        .togetherWith(slideOutVertically(tween(180, easing = FastOutLinearInEasing)) { it / 4 } + fadeOut(tween(180)))
                }
                // Navigating to/from SETTINGS: slide from right
                targetState == ScreenState.SETTINGS -> {
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 500f)) { it / 4 } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(tween(160)) { -it / 6 } + fadeOut(tween(160)))
                }
                initialState == ScreenState.SETTINGS -> {
                    (slideInHorizontally(tween(180)) { -it / 6 } + fadeIn(tween(180)))
                        .togetherWith(slideOutHorizontally(tween(180, easing = FastOutLinearInEasing)) { it / 4 } + fadeOut(tween(180)))
                }
                // Navigating to/from WIDGETS DECK: instant switch with zero animation
                targetState == ScreenState.WIDGETS || initialState == ScreenState.WIDGETS -> {
                    EnterTransition.None.togetherWith(ExitTransition.None)
                }
                // Navigating to/from HUB: instant switch with zero animation
                targetState == ScreenState.HUB || initialState == ScreenState.HUB -> {
                    EnterTransition.None.togetherWith(ExitTransition.None)
                }
                else -> {
                    fadeIn(tween(220)).togetherWith(fadeOut(tween(180)))
                }
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            ScreenState.ONBOARDING -> {
                OnboardingScreen(
                    userPreferences = userPreferences,
                    onFinish = {
                        scope.launch {
                            userPreferences.setOnboardingCompleted(true)
                        }
                        currentScreen = ScreenState.HOME
                    }
                )
            }

            ScreenState.HOME -> {
                HomeScreen(
                    installedApps = installedApps,
                    storageRepository = storageRepository,
                    contactRepository = contactRepository,
                    userPreferences = userPreferences,
                    executeCommandUseCase = executeCommandUseCase,
                    appWidgetManager = appWidgetManager,
                    appWidgetHost = appWidgetHost,
                    onAddHomeWidget = onAddHomeWidget,
                    onDeleteHomeWidget = onDeleteHomeWidget,
                    onNavigateToApps = { currentScreen = ScreenState.APPS },
                    onNavigateToHub = { currentScreen = ScreenState.HUB },
                    onNavigateToWidgets = { currentScreen = ScreenState.WIDGETS },
                    onNavigateToSettings = { currentScreen = ScreenState.SETTINGS },
                    onOpenMemo = { showMemoSheet = true },
                    onOpenClock = { showClockSheet = true }
                )
            }

            ScreenState.APPS -> {
                AppDrawerScreen(
                    apps = installedApps,
                    favoriteAppPackages = favoriteAppPackages,
                    onTogglePin = { app ->
                        scope.launch {
                            val updated = if (app.packageName in favoriteAppPackages) {
                                favoriteAppPackages - app.packageName
                            } else {
                                val currentHomeWidgets = userPreferences.homeWidgets.first()
                                if ("SHORTCUTS" !in currentHomeWidgets) {
                                    userPreferences.setHomeWidgets(currentHomeWidgets + "SHORTCUTS")
                                }
                                favoriteAppPackages + app.packageName
                            }
                            userPreferences.setFavoriteApps(updated)
                        }
                    },
                    onUninstall = { app ->
                        executeCommandUseCase.execute("uninstall ${app.packageName}")
                    },
                    onBack = { currentScreen = ScreenState.HOME }
                )
            }

            ScreenState.HUB -> {
                HubScreen(
                    userPreferences = userPreferences,
                    onBackToHome = { currentScreen = ScreenState.HOME }
                )
            }

            ScreenState.WIDGETS -> {
                WidgetsDeckScreen(
                    storageRepository = storageRepository,
                    userPreferences = userPreferences,
                    executeCommandUseCase = executeCommandUseCase,
                    systemWidgetIds = deckWidgetIds,
                    appWidgetManager = appWidgetManager,
                    appWidgetHost = appWidgetHost,
                    onAddSystemWidget = onAddDeckWidget,
                    onDeleteSystemWidget = onDeleteDeckWidget,
                    onBackToHome = { currentScreen = ScreenState.HOME }
                )
            }

            ScreenState.SETTINGS -> {
                SettingsScreen(
                    installedApps = installedApps,
                    userPreferences = userPreferences,
                    onNavigateToOnboarding = { currentScreen = ScreenState.ONBOARDING },
                    onBack = { currentScreen = ScreenState.HOME }
                )
            }
        }
    }

    // Modal Mini Apps Bottom Sheets
    if (showMemoSheet) {
        MinimalMemoSheet(
            storageRepository = storageRepository,
            executeCommandUseCase = executeCommandUseCase,
            onDismiss = { showMemoSheet = false }
        )
    }

    if (showClockSheet) {
        MinimalClockSheet(
            onDismiss = { showClockSheet = false }
        )
    }
}
