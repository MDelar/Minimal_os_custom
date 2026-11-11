package com.zaryif.minimalos.util

/**
 * ═════════════════════════════════════════════════════════════════════════════
 *  CENTRALIZED COPY & STRING REGISTRY FOR MINIMAL OS
 * ═════════════════════════════════════════════════════════════════════════════
 *  Change ANY text displayed in the app directly from this single file.
 *  Titles, subtitles, buttons, tab labels, search hints, settings sections,
 *  widget deck labels, and dialog texts are all declared here as easily
 *  editable properties.
 * ═════════════════════════════════════════════════════════════════════════════
 */
object AppStrings {

    // ── 1. App Identity & Top Bar ───────────────────────────────────────────
    var appName = "Minimal OS"
    var homeTopBrand = "minimal"

    // ── 2. Hub Screen (Notifications & Alerts Stream) ───────────────────────
    var hubTitle = "Hub"
    var hubSubtitleLive = "Live notification stream"
    var hubSubtitleArchive = "Archived notifications"

    // Hub Tabs & Filters
    var hubTabLive = "Live"
    var hubTabArchive = "Archive"
    var hubCategoryAll = "ALL"
    var hubCategoryMessages = "MESSAGE"
    var hubCategoryCalls = "CALLS"
    var hubCategoryEmail = "EMAIL"

    // Hub Search
    var hubSearchPlaceholder = "Search alerts by app, title, or message…"

    // Hub Actions & Buttons
    var hubAiSummarizeCard = "Summarize alerts with AI Assistant"
    var hubDownloadLogBtn = "Log"
    var hubClearBtn = "Clear"
    var hubClearAllBtn = "Clear All"
    var hubHomeBtn = "Home"

    // Hub Empty States
    var hubEmptyTitle = "All caught up"
    var hubEmptySubtitle = "Incoming alerts and messages stream here live"
    var hubEmptyArchiveTitle = "Archive is empty"
    var hubEmptyArchiveSubtitle = "Archived notifications are safely stored here"

    // Hub Permission Banner
    var hubPermissionTitle = "Notification access required"
    var hubPermissionSubtitle = "Tap to grant permission so Hub can receive incoming alerts"

    // Hub Now Playing Card
    var hubNowPlayingTitle = "NOW PLAYING"
    var hubPausedTitle = "PAUSED"

    // Hub Dialogs
    var hubClearDialogTitle = "Clear notification archive?"
    var hubClearDialogBody = "This will permanently delete all stored local notifications from your device."
    var hubClearDialogConfirm = "Clear All"
    var hubClearDialogCancel = "Cancel"
    var hubExportDialogTitle = "Hub Intelligence & Export"
    var hubExportDialogDesc = "Summarize or export notification logs for AI analysis:"
    var hubExportToAssistantTitle = "Send to AI Assistant"
    var hubExportToAssistantSubtitle = "Formats full log with executive summary prompt"
    var hubExportTxtTitle = "Share plain text log"
    var hubExportTxtSubtitle = "Share log via messaging or storage apps"
    var hubExportDownloadTitle = "Save to Downloads"
    var hubExportDownloadSubtitle = "Saves file directly to device Downloads"
    var hubExportCopyTitle = "Copy prompt & log"
    var hubExportCopySubtitle = "Copies formatted prompt to clipboard"

    // ── 3. Quick Note Widget & Fullscreen Sheet ─────────────────────────────
    var quickNoteTitle = "Quick Note"
    var quickNotePinBtn = "Pin"
    var quickNoteClearBtn = "Clear"
    var quickNoteDoneBtn = "Done"
    var quickNoteAiSummarize = "AI Summarize"
    var quickNotePlaceholder = "Type note here · press enter or pin to status bar…"
    var quickNoteSheetPlaceholder = "What's on your mind? Jot anything here…\n\nWhatever you write stays persistently pinned to your status bar so you never forget."
    var quickNoteAiPromptPrefix = "Please summarize, organize, and extract key action items from this quick note from Minimal OS:\n\n"

    // ── 4. Minimal Memo Sheet (Tasks, Notes & Pinned Note) ──────────────────
    var memoTitle = "Memo"
    var memoTabTasks = "Tasks"
    var memoTabNotes = "Notes"
    var memoTabQuickNote = "Quick Note"
    var memoTimerBtn = "TIMER"

    // Tasks & Notes Search & Add
    var memoSearchTasksHint = "Search tasks…"
    var memoSearchNotesHint = "Search notes…"
    var memoAddTaskHint = "Add new task (press enter)…"
    var memoAddNoteHint = "Write new note (press enter)…"

    // Empty States
    var memoEmptyTasks = "No tasks yet. Type above to add your first task."
    var memoEmptyNotes = "No notes yet. Type above to write your first note."

    // Pinned Note Status in Memo
    var memoPinnedActive = "PINNED TO NOTIFICATION BAR"
    var memoPinnedEmpty = "NOT PINNED (EMPTY)"
    var memoSummarizeWithAi = "Summarize note with AI Assistant"
    var memoQuickNotePlaceholder = "Type quick note here…\n\nWhatever you write stays persistently pinned to your status bar so you never forget.\n\nTap below to send to your AI Assistant for an instant organized summary."

    // ── 5. Home Navigation Hints & Command Bar ──────────────────────────────
    var navHub = "<- hub"
    var navApps = "↑ apps"
    var navWidgets = "widgets ->"
    var commandBarPlaceholder = "type command or search apps..."

    // ── 6. Settings Screen (All Sections & Functions) ───────────────────────
    var settingsTitle = "settings"
    var settingsSubtitle = "system · appearance · shortcuts · cards"
    var settingsSearchPlaceholder = "Search settings…"

    // Section 1: Appearance
    var settingsSecAppearance = "APPEARANCE & PERSONALIZATION"
    var settingsThemeTitle = "Themes & Accents"
    var settingsWallpaperTitle = "Wallpaper & Style"

    // Section 2: Home Screen & Shortcuts
    var settingsSecHome = "HOME SCREEN & SHORTCUTS"
    var settingsPinnedShortcutsTitle = "Pinned App Shortcuts"
    var settingsHomeWidgetsTitle = "Home Widgets & Order"
    var settingsShowAllTasksTitle = "Show All Tasks on Home"
    var settingsShowAllNotesTitle = "Show All Notes on Home"

    // Section 3: Widgets Deck
    var settingsSecDeck = "WIDGETS DECK (PAGE 3)"
    var settingsDeckCardsTitle = "Deck Cards Arrangement"

    // Section 4: Command Bar & History
    var settingsSecCommand = "COMMAND BAR & SHORTCUTS"
    var settingsTerminalBufferIconTitle = "Terminal Buffer Icon"
    var settingsTerminalBufferIconDesc = "Show terminal history buffer toggle icon inside command box"
    var settingsCommandHelperIconTitle = "Command Helper Icon"
    var settingsCommandHelperIconDesc = "Show '?' cheat sheet button inside command box"
    var settingsAliasesTitle = "Command Aliases"
    var settingsFeedbackTitle = "Action Confirmation Popups"
    var settingsFeedbackDesc = "Show transient 'Saved task' / 'Saved note' banner"
    var settingsHistoryTitle = "Command Box History"
    var settingsExportHistoryTitle = "Download History (.md)"

    // Section 5: System & Assistant
    var settingsSecSystem = "SYSTEM & ASSISTANT"
    var settingsAssistantTitle = "Digital Assistant"
    var settingsLauncherTitle = "Default Home Launcher"
    var settingsHubAccessTitle = "Notification Hub Access"
    var settingsSaveHistoryTitle = "Save Notification History"
    var settingsDownloadHistoryTitle = "Download Notification History (.txt)"

    // Section 6: About
    var settingsSecAbout = "ABOUT & INFORMATION"
    var settingsAboutLauncherTitle = "Minimal OS Launcher"
    var settingsAboutLauncherSubtitle = "Tap for architecture & details"
    var settingsCommandGuideTitle = "Command Reference & Guide"
    var settingsCommandGuideSubtitle = "Browse all 16 command symbols, custom aliases, and natural syntax"
    var settingsDeveloperTitle = "Developer & Creator"
    var settingsDeveloperSubtitle = "MD. Zarif Azfar (@zaryif) · Dhaka, Bangladesh"
    var settingsSupportTitle = "Support the Project"
    var settingsSupportSubtitle = "buymeacoffee.com/zaryif · Fund open-source development"
    var settingsWalkthroughTitle = "App Walkthrough & Guide"
    var settingsWalkthroughSubtitle = "Interactive tutorial on command gestures, hub, & philosophy"
    var settingsResetOnboardingTitle = "Reset Onboarding Experience"
    var settingsResetOnboardingSubtitle = "Clear completed state and restart the 5-step walkthrough"
    var settingsGithubTitle = "GitHub Releases & Source Code"
    var settingsGithubSubtitle = "github.com/zaryif/Minimal_os · Latest APKs & commits"
    var settingsReplayWizardTitle = "Replay Setup Wizard"

    // ── 7. Widgets Deck Screen & Home Widgets ───────────────────────────────
    var deckTitle = "widgets"
    var deckSubtitle = "interactive cards · notes · widgets · focus"
    var deckHomeBtn = "HOME ->"
    var deckSwipeRightHint = "SWIPE RIGHT FOR HOME ->"
    var deckAddWidgetBtn = "+ ADD WIDGET"
    var deckNoSystemWidgets = "No external app widgets added yet.\nTap '+ ADD WIDGET' to host Calendar, Spotify, Weather, etc."

    var widgetTasksTitle = "tasks"
    var widgetNotesTitle = "notes"
    var widgetCalendarTitle = "calendar"
    var widgetCalendarSubtitle = "view schedule & events"
    var widgetShortcutsTitle = "shortcuts"
    var widgetNextAlarmTitle = "NEXT ALARM"
    var widgetAlarmActive = "● ACTIVE"
    var widgetAlarmStandby = "● STANDBY"
    var widgetNoUpcomingAlarm = "NO UPCOMING ALARM"
    var widgetConfigureAlarmHint = "Tap to configure an alarm in deck"
    var widgetClockPermissionNeeded = "CLOCK PERMISSION NEEDED"
    var widgetGrantPermissionHint = "Tap to grant exact alarm permission"
    var widgetGrantBtn = "GRANT"
    var widgetPomodoroTitle = "pomodoro"
    var widgetTimersTitle = "timers"
    var widgetAliasesTitle = "aliases & shortcuts"
    var widgetOpenMemo = "open memo"
    var widgetCollapse = "collapse"
    var widgetAllApps = "all apps"
    var widgetNoAlarm = "no upcoming alarm"
    var widgetConfigureAlarm = "tap to configure an alarm"
    var widgetAddWidgetPlaceholder = "+ Add system app widget (Spotify, Weather, Calendar)"
}
