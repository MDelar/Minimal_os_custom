# Minimal OS · Full Codebase Quality Review & Bug Audit

**Conducted with:** `/ponytail-review`  
**Target:** Entire Minimal OS Android Codebase (`com.zaryif.minimalos`)  
**Expected Load:** Continuously running Android primary home launcher handling daily user commands, status bar notifications, widget updates, media sessions, alarms, gestures, and persistent local storage.

---

### What this repo does:
Minimal OS is an ultra-minimalist, typography-driven Android launcher designed to eliminate smartphone distraction. It replaces app icon grids with a terminal-like natural language command bar, a live Notification Hub with media playback controls and persistent local archiving, an app drawer with pin/uninstall capabilities, and customizable widget decks.

---

## Must Fix
*Bugs, crashes, data loss, sleep/clock drift, and broken workflows.*

### 1. Uncaught `ActivityNotFoundException` on App Launch
- **Location:** [`AppDrawerScreen.kt:146, 228`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/apps/AppDrawerScreen.kt#L146) and [`HomeScreen.kt:538`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/home/HomeScreen.kt#L538)
- **What this is:** Launches an application when the user taps an app in the App Drawer, presses Enter in app search, or taps a pinned home shortcut.
- **Problem:** If an application is uninstalled, disabled, or package info is stale, calling `context.startActivity(launchIntent)` directly without a `try/catch` throws an unhandled `ActivityNotFoundException` or `SecurityException`, crashing the launcher immediately to the Android lock screen.
- **Fix:** Wrap `context.startActivity(it)` in `try { ... } catch (e: Exception) { ... }` with a friendly error toast or banner.
- **If we skip it:** Minimal OS crashes whenever a user taps an app that was recently updated, disabled, or removed in the background.

---

### 2. Pomodoro & Countdown Timer Freeze / Drift During Sleep
- **Location:** [`PomodoroTimerManager.kt:111-126`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/domain/timer/PomodoroTimerManager.kt#L111-L126)
- **What this is:** Global timer engine powering the Pomodoro focus widget and countdown timers.
- **Problem:** The countdown loop relies on `delay(1000)` and decrements `_remainingSeconds.value--`. When the user locks their screen or the phone enters Android Doze/sleep, coroutine delays pause. A 25-minute timer only ticks a few seconds while locked, and alarms fail to fire when the time elapses.
- **Fix:** Record `targetEndTime = System.currentTimeMillis() + seconds * 1000L` on start. In the ticker, calculate remaining seconds from the wall clock: `((targetEndTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)`.
- **If we skip it:** The timer pauses every time the phone screen turns off, rendering focus sessions and alarms completely unreliable.

---

### 3. Custom Wallpaper Out-of-Memory Crash & Storage Leak
- **Location:** [`HomeScreen.kt:357-360`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/home/HomeScreen.kt#L357-L360) and [`SettingsScreen.kt:190`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/settings/SettingsScreen.kt#L190)
- **What this is:** User photo gallery custom wallpaper decoding and storage.
- **Problem:** When picking a custom wallpaper, `HomeScreen.kt` decodes content stream URIs without `inSampleSize` downsampling. A 48MP camera photo (8000×6000) allocates ~192MB of uncompressed bitmap memory on the UI thread, causing an immediate `OutOfMemoryError` crash. Additionally, `SettingsScreen.kt` writes `custom_wallpaper_${System.currentTimeMillis()}.jpg` on each pick without deleting old files, leaking dozens of megabytes in internal storage.
- **Fix:** Downsample bitmaps to screen bounds (`inSampleSize`) and overwrite a single fixed `custom_wallpaper.jpg` file in `context.filesDir`.
- **If we skip it:** Setting high-resolution gallery wallpapers crashes the launcher with OOM, and internal storage fills up over time.

---

### 4. Blank-Key Overwrite Bug in Local Notification Archive
- **Location:** [`NotificationHistoryRepository.kt:47-75`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/data/repository/NotificationHistoryRepository.kt#L47-L75)
- **What this is:** Local SQLite database archiving all notifications with `sbn_key TEXT UNIQUE` and `CONFLICT_REPLACE`.
- **Problem:** If incoming notifications have an empty or null key (`""`), inserting a second notification with `""` replaces the previous one because of the unique constraint and conflict replacement.
- **Fix:** Fallback to a deterministic compound key: `val safeKey = item.key.ifBlank { "${item.packageName}_${item.id}_${item.timestamp}" }`.
- **If we skip it:** Notifications without explicit keys overwrite each other, silently losing historical logs.

---

### 5. Note Command Auto-Foregrounds Google Keep
- **Location:** [`ExecuteCommandUseCase.kt:287-308`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/domain/usecase/ExecuteCommandUseCase.kt#L287-L308)
- **What this is:** Handling quick notes via `- Buy groceries` or `note <text>`.
- **Problem:** If Google Keep is installed, `handleNote()` immediately calls `context.startActivity(keepIntent)`. This launches Google Keep into the foreground on top of Minimal OS, abruptly interrupting the user when they simply wanted to add a note to their launcher's memo widget.
- **Fix:** Keep the note in Minimal OS local storage only; remove the automatic foreground `startActivity(keepIntent)` unless explicitly requested.
- **If we skip it:** Every quick note entered in the command bar boots the user out of the launcher into Google Keep.

---

## Should Fix
*Performance bottlenecks, main-thread IPC locks, gesture conflicts, and code duplication.*

### 6. Main-Thread App Icon Decoding on Every Resume
- **Location:** [`AppRepository.kt:21-29`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/data/repository/AppRepository.kt#L21-L29) and [`MainActivity.kt:233`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/MainActivity.kt#L233)
- **What this is:** `getInstalledApps()` queries all launchable applications and decodes their app icons.
- **Problem:** On every `ON_RESUME` (every time user returns Home), `resolveInfo.loadIcon(pm)` is executed synchronously on the main thread for 150–300 apps. However, `AppInfo.icon` is literally never rendered anywhere in Minimal OS (pure typography design).
- **Fix:** Remove `resolveInfo.loadIcon(pm)` from `getInstalledApps()` or make it lazy/null.
- **If we skip it:** Launcher stutters and suffers frame drops every time the user exits an app back to Home.

---

### 7. Synchronous Contact ContentProvider IPC on Every Keystroke
- **Location:** [`HomeScreen.kt:185-197`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/home/HomeScreen.kt#L185-L197)
- **What this is:** Live contact matching for `@name`, `call`, `sms`, and `wa` commands.
- **Problem:** `remember(query, hasContactPermission)` calls `contactRepository.searchContacts()` synchronously on the UI thread during Compose recomposition. Querying `ContactsContract` over Binder IPC takes 15–80ms per character typed.
- **Fix:** Debounce and offload contact search to `Dispatchers.IO` via `produceState` or a coroutine flow.
- **If we skip it:** Typing contact names into the command bar hitches the keyboard and drops frames.

---

### 8. Continuous 1-Second IPC Polling on `AlarmManager`
- **Location:** [`WidgetsDeckScreen.kt:998-1005`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/widgets/WidgetsDeckScreen.kt#L998-L1005) and [`MinimalHomeWidgets.kt:546-553`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/components/MinimalHomeWidgets.kt#L546-L553)
- **What this is:** Live upcoming alarm countdown on the Home screen and Widgets Deck.
- **Problem:** An infinite `while(true)` coroutine loop polls `AlarmManager.nextAlarmClock` via binder IPC every 1,000ms. System alarms do not change every second; continuous IPC wakeups waste CPU cycles and battery.
- **Fix:** Query `nextAlarmClock` once on screen entry (or listen to `ACTION_NEXT_ALARM_CLOCK_CHANGED`), and decrement `currentTime` locally.
- **If we skip it:** Unnecessary battery drain and background binder load while on the Home screen.

---

### 9. Root Drag Gesture Blocks Vertical Widget Scrolling
- **Location:** [`HomeScreen.kt:389-401`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/home/HomeScreen.kt#L389-L401)
- **What this is:** Horizontal swipe detection for navigating to Hub (`x > 50`) and Widgets Deck (`x < -50`).
- **Problem:** The root `Column` uses `detectDragGestures { ... }`, which consumes pointer events before the nested `verticalScroll(rememberScrollState())` can receive them. This makes scrolling widgets on Home feel sticky or unresponsive.
- **Fix:** Replace `detectDragGestures` with `detectHorizontalDragGestures` so vertical drag events pass through freely to `verticalScroll`.
- **If we skip it:** Scrolling up and down through widgets on the home screen feels stiff and often fails to register.

---

### 10. DataStore Main-Thread `runBlocking` in Command Execution
- **Location:** [`ExecuteCommandUseCase.kt:52, 602, 644, 715, 973, 999`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/domain/usecase/ExecuteCommandUseCase.kt#L52)
- **What this is:** Fetching custom aliases, favorite apps, and assistant package settings during command execution.
- **Problem:** Uses `runBlocking { userPreferences.xxx.first() }` on the main UI thread. If DataStore disk I/O blocks, it freezes the UI frame or triggers an ANR.
- **Fix:** Cache preferences in memory or make `execute()` suspend and launch within a UI coroutine scope.
- **If we skip it:** Occasional UI freeze when executing commands that touch DataStore.

---

### 11. First-Launch Screen Flash: Null State Misrouting
- **Location:** [`MainActivity.kt:253-257`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/MainActivity.kt#L253-L257)
- **What this is:** Startup routing between `ScreenState.ONBOARDING` and `ScreenState.HOME`.
- **Problem:** `onboardingCompleted` flow starts with `initial = null`. The `when (onboardingCompleted)` maps `null -> ScreenState.HOME`. On fresh installs, Home displays for 1–2 frames before DataStore emits `false`, causing a visible flash of Home before switching to Onboarding.
- **Fix:** When `onboardingCompleted == null`, show a neutral black background until the DataStore state resolves.
- **If we skip it:** New users see a glitchy frame of the home screen before onboarding begins.

---

### 12. 1,796-Line Widgets Deck Duplication
- **Location:** [`WidgetsDeckScreen.kt`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/widgets/WidgetsDeckScreen.kt) vs [`MinimalHomeWidgets.kt`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/components/MinimalHomeWidgets.kt)
- **What this is:** Two completely independent implementations of Notes, Todos, Calendar, Pomodoro, and Alarm cards (`Rabbit...Card` vs `MinimalHome...Widget`).
- **Problem:** Duplicate state machines, duplicate UI code, and inconsistent behavior between the Home screen and the Deck.
- **Fix:** Extract shared card components into reusable UI components, shrinking `WidgetsDeckScreen.kt` by ~800 lines.
- **If we skip it:** Double maintenance burden; bug fixes applied on Home do not propagate to the Deck.

---

## Nice to Have
*Micro-optimizations, cleaner code, and dead code cleanup.*

### 13. Redundant Visual Transformation Checking
- **Location:** [`CommandBar.kt:139-169`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/ui/components/CommandBar.kt#L139-L169)
- **What this is:** Highlighting action keywords in the command bar.
- **Problem:** Checks 30 string conditions with `it == ... || it.startsWith(...)` on every frame instead of checking `firstToken in BUILTIN_ACTION_KEYWORDS` (which is already defined on line 86).
- **Fix:** Replace the 30-line chain with `firstToken in BUILTIN_ACTION_KEYWORDS`.
- **If we skip it:** Minor CPU overhead during typing.

---

### 14. Repeated Regex and Prefix Allocations
- **Location:** [`CommandParser.kt:66-83`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/domain/parser/CommandParser.kt#L66-L83)
- **What this is:** Command keyword prefix mappings inside `CommandParser.parse()`.
- **Problem:** Reallocates a list of 16 `PrefixMapping` objects and string arrays on every single keystroke.
- **Fix:** Move `keywordMatches` to a `private val KEYWORD_MATCHES` top-level constant.
- **If we skip it:** Small GC garbage churn on keyboard input.

---

### 15. Media Session Listener Leak on Repeated Init
- **Location:** [`MinimalMediaManager.kt:73`](file:///Users/zaryif/Minimal_os/app/src/main/java/com/zaryif/minimalos/domain/media/MinimalMediaManager.kt#L73)
- **What this is:** Registering `OnActiveSessionsChangedListener` with `MediaSessionManager`.
- **Problem:** If `init(context)` is called more than once without `cleanup()`, listeners stack up.
- **Fix:** Add `if (sessionListener != null) return` before registering.
- **If we skip it:** Duplicate media session callbacks over time.

---

## Summary Verdict

| Category | Count | Status |
| :--- | :---: | :--- |
| **Must Fix** | 5 | Critical bugs (OOM crash, timer sleep freeze, crash on missing apps, Keep hijacking) |
| **Should Fix** | 7 | Real bottlenecks (main-thread icon decode, contact IPC jank, sticky gestures, `runBlocking`) |
| **Nice to Have** | 3 | Code cleanup, allocation reductions, listener leak protection |

- **Verdict:** Fix 1, 2, 3, and 5 first.
- **Lean:** `-850` lines possible (by consolidating `WidgetsDeckScreen.kt` and deleting unused icon decoding).
- **Not checked:** OEM-specific background battery-killing policies (Xiaomi MIUI, Huawei EMUI, Samsung battery optimization) which may terminate `MinimalNotificationService` if battery unrestricted mode is not granted.

