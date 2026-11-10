# Minimal OS: Aliases and Shortcuts Directory

A comprehensive reference directory of 120+ ready-to-use custom aliases, Android system settings actions, web query templates, app launch shortcuts, and command macros for Minimal OS Launcher.

---

## Table of Contents

1. [Overview](#1-overview)
2. [How to Create and Manage Aliases](#2-how-to-create-and-manage-aliases)
   - [Method 1: Direct Command Bar (CLI)](#method-1-direct-command-bar-cli)
   - [Method 2: Settings UI (Visual Manager)](#method-2-settings-ui-visual-manager)
   - [Method 3: Widgets Deck (1-Tap Card)](#method-3-widgets-deck-1-tap-card)
3. [Alias Execution Engine Architecture](#3-alias-execution-engine-architecture)
4. [Official Sources and Reference Documentation](#4-official-sources-and-reference-documentation)
   - [Where to Find Android Settings Actions](#where-to-find-android-settings-actions)
   - [Where to Find App Package IDs](#where-to-find-app-package-ids)
   - [Where to Find Web Search URL Templates](#where-to-find-web-search-url-templates)
5. [Directory of 120+ Aliases and Shortcuts](#5-directory-of-120-aliases-and-shortcuts)
   - [Category A: Network and Wireless Settings (15)](#category-a-network-and-wireless-settings)
   - [Category B: Display, Sound, and Zen Mode (15)](#category-b-display-sound-and-zen-mode)
   - [Category C: System, Battery, and Privacy (18)](#category-c-system-battery-and-privacy)
   - [Category D: App Management and Permissions (15)](#category-d-app-management-and-permissions)
   - [Category E: Accessibility and Input Devices (10)](#category-e-accessibility-and-input-devices)
   - [Category F: Web Search and Query Engines (25)](#category-f-web-search-and-query-engines)
   - [Category G: App Launch and Package Shortcuts (15)](#category-g-app-launch-and-package-shortcuts)
   - [Category H: Minimal OS Command Macros (15)](#category-h-minimal-os-command-macros)
6. [Troubleshooting and OEM Compatibility](#6-troubleshooting-and-oem-compatibility)

---

## 1. Overview

Minimal OS is built around intentional, keyboard-driven navigation. Instead of swiping through multi-layered app drawers or digging through nested system settings pages, the **Minimal OS Command Bar** allows users to define custom shorthand keywords called **aliases**.

An alias maps a short trigger keyword (such as `wifi`, `gh`, `wa`, `pomo50`) to:
- A native **Android System Settings Intent** (e.g., `android.settings.WIFI_SETTINGS`).
- A dynamic **Web Search Template** with argument expansion (e.g., `https://github.com/search?q={query}`).
- An **Installed App** via package name or label (e.g., `com.termux`).
- A **Minimal OS Command Macro** (e.g., `pomo 25`, `timer 10m`, `alarm 07:00`, `- buy milk`).

---

## 2. How to Create and Manage Aliases

Minimal OS provides three distinct ways to configure aliases depending on your preferred workflow.

### Method 1: Direct Command Bar (CLI)

You can create, inspect, and delete aliases directly in the main Command Bar on your home screen without navigating into settings:

- **Create or Update an Alias**:
  ```text
  alias <name> = <target>
  ```
  *Examples*:
  ```text
  alias wifi = android.settings.WIFI_SETTINGS
  alias gh = https://github.com/search?q={query}
  alias pomo50 = pomo 50
  alias termux = com.termux
  ```

- **List All Active Aliases**:
  ```text
  alias
  ```
  or:
  ```text
  alias list
  ```
  Displays all configured custom aliases line-by-line.

- **Delete an Alias**:
  ```text
  unalias <name>
  ```
  or:
  ```text
  rmalias <name>
  ```
  *Example*:
  ```text
  unalias wifi
  ```

### Method 2: Settings UI (Visual Manager)

1. Open **Settings** (type `settings` in the command bar or tap the Settings gear icon).
2. Scroll to the **Custom Aliases & Shortcuts** card.
3. Tap **Add Alias** to enter a keyword and its target.
4. Existing aliases are displayed with 1-tap delete buttons and edit controls.

### Method 3: Widgets Deck (1-Tap Card)

1. From the home screen, swipe left into the **Widgets Deck**.
2. Locate the **Rabbit Aliases & Shortcuts** card.
3. Tap any alias chip to trigger it immediately with a single tap.
4. Use the inline quick-add input bar to create new shortcuts on the fly.

---

## 3. Alias Execution Engine Architecture

When an alias is typed into the Command Bar, Minimal OS evaluates it in `ExecuteCommandUseCase.kt` according to the following order of precedence:

```
[Raw User Input]
       │
       ▼
[Check Custom Aliases Map in DataStore]
       │
       ├─► Target starts with "android.settings." or "android.provider.Settings."
       │   └─► Launch Android System Settings Intent (ACTION_VIEW / Specific Action)
       │
       ├─► Target starts with "http://" or "https://"
       │   └─► If URL contains "{query}", replace with remaining arguments.
       │   └─► If URL does not contain "{query}", append arguments or open URL.
       │
       ├─► Target starts with a launcher command trigger ('-', '!', 'pomo', 'timer', etc.)
       │   └─► Re-evaluate recursively as a native Minimal OS command.
       │
       ├─► Target matches an installed Android Package Name or App Label
       │   └─► Launch app via PackageManager.getLaunchIntentForPackage()
       │
       └─► Fallback: Web search with target and arguments.
```

---

## 4. Official Sources and Reference Documentation

### Where to Find Android Settings Actions

Android system settings are exposed as public `Intent` action strings defined in `android.provider.Settings`. All standard Android OS versions (from Android 8.0 Oreo through Android 15+) support these intents out-of-the-box.

- **Official Android Documentation**:
  - `android.provider.Settings` Reference: [https://developer.android.com/reference/android/provider/Settings](https://developer.android.com/reference/android/provider/Settings)
  - General Intent Actions Reference: [https://developer.android.com/reference/android/content/Intent](https://developer.android.com/reference/android/content/Intent)

*How to convert Android documentation constants to Minimal OS aliases*:
In Android documentation, constants are written as `Settings.ACTION_WIFI_SETTINGS`. The actual string value is `android.settings.WIFI_SETTINGS`. In Minimal OS, you can provide either:
- The standard string: `android.settings.WIFI_SETTINGS`
- The provider format: `android.provider.Settings.ACTION_WIFI_SETTINGS` (Minimal OS automatically normalizes this prefix).

### Where to Find App Package IDs

Every Android application has a unique application ID (reverse domain notation, e.g., `com.whatsapp`, `org.videolan.vlc`).

To find the exact package ID for any app:

1. **Google Play Store URL**:
   Browse to the app on Google Play ([https://play.google.com/store/apps](https://play.google.com/store/apps)). Look at the URL parameter `?id=`:
   - URL: `https://play.google.com/store/apps/details?id=com.spotify.music`
   - Package ID: `com.spotify.music`

2. **F-Droid / Aurora Store**:
   Browse on F-Droid ([https://f-droid.org](https://f-droid.org)). The URL ends with the package ID:
   - URL: `https://f-droid.org/packages/com.termux/`
   - Package ID: `com.termux`

3. **On-Device Terminal (ADB or Termux)**:
   List third-party installed packages on your phone:
   ```bash
   pm list packages -3
   ```
   Or filter for a specific app:
   ```bash
   pm list packages | grep -i obsidian
   ```

4. **Minimal OS Command Bar**:
   Typing the app label (e.g. `Spotify`) into the alias target works directly because Minimal OS resolves app labels via `PackageManager`. Using the package ID (`com.spotify.music`) is even faster and guarantees 100% deterministic matching.

### Where to Find Web Search URL Templates

Web services that accept query strings via `GET` requests can be turned into instant search engines by replacing the search parameter with `{query}`.

- To find the search URL for any website:
  1. Open the website in your browser.
  2. Search for the word `TEST`.
  3. Look at your browser address bar.
  4. Replace `TEST` with `{query}`.
  - *Example*: Searching `TEST` on GitHub yields `https://github.com/search?q=TEST`.
  - *Minimal OS Alias*: `alias gh = https://github.com/search?q={query}`.

---

## 5. Directory of 120+ Aliases and Shortcuts

Below is an extensive collection of tested shortcuts ready to be pasted into Minimal OS.

---

### Category A: Network and Wireless Settings

| Alias Command | Target Action | Purpose / Description |
| :--- | :--- | :--- |
| `alias wifi = android.settings.WIFI_SETTINGS` | `android.settings.WIFI_SETTINGS` | Direct access to Wi-Fi networks and connection controls |
| `alias wifisaved = android.settings.WIFI_SAVED_NETWORK_SETTINGS` | `android.settings.WIFI_SAVED_NETWORK_SETTINGS` | Manage saved Wi-Fi SSIDs and credentials |
| `alias wifiip = android.settings.WIFI_IP_SETTINGS` | `android.settings.WIFI_IP_SETTINGS` | Advanced Wi-Fi static IP, DNS, and gateway settings |
| `alias bt = android.settings.BLUETOOTH_SETTINGS` | `android.settings.BLUETOOTH_SETTINGS` | Bluetooth device pairing, scanning, and connections |
| `alias airplane = android.settings.AIRPLANE_MODE_SETTINGS` | `android.settings.AIRPLANE_MODE_SETTINGS` | Toggle Airplane Mode radio states |
| `alias wireless = android.settings.WIRELESS_SETTINGS` | `android.settings.WIRELESS_SETTINGS` | Network & Internet top-level system screen |
| `alias data = android.settings.DATA_ROAMING_SETTINGS` | `android.settings.DATA_ROAMING_SETTINGS` | Cellular network, roaming, and SIM card management |
| `alias datausage = android.settings.DATA_USAGE_SETTINGS` | `android.settings.DATA_USAGE_SETTINGS` | Data usage monitoring, data warning, and limits |
| `alias hotspot = android.settings.TETHER_SETTINGS` | `android.settings.TETHER_SETTINGS` | Wi-Fi hotspot, USB tethering, and Bluetooth tethering |
| `alias vpn = android.settings.VPN_SETTINGS` | `android.settings.VPN_SETTINGS` | System VPN configurations and tunnels |
| `alias nfc = android.settings.NFC_SETTINGS` | `android.settings.NFC_SETTINGS` | NFC toggle and contactless payment configuration |
| `alias nfcshare = android.settings.NFCSHARING_SETTINGS` | `android.settings.NFCSHARING_SETTINGS` | NFC direct file sharing and Beam options |
| `alias apn = android.settings.APN_SETTINGS` | `android.settings.APN_SETTINGS` | Access Point Names for carrier mobile internet setup |
| `alias network = android.settings.NETWORK_OPERATOR_SETTINGS` | `android.settings.NETWORK_OPERATOR_SETTINGS` | Manual cellular network operator selection |
| `alias cast = android.settings.CAST_SETTINGS` | `android.settings.CAST_SETTINGS` | Wireless screen cast, Miracast, and Google Cast |

---

### Category B: Display, Sound, and Zen Mode

| Alias Command | Target Action | Purpose / Description |
| :--- | :--- | :--- |
| `alias display = android.settings.DISPLAY_SETTINGS` | `android.settings.DISPLAY_SETTINGS` | Screen brightness, screen timeout, font size, and themes |
| `alias nightlight = android.settings.NIGHT_DISPLAY_SETTINGS` | `android.settings.NIGHT_DISPLAY_SETTINGS` | Blue light filter, night mode schedule, and warmth |
| `alias rotate = android.settings.AUTO_ROTATE_SETTINGS` | `android.settings.AUTO_ROTATE_SETTINGS` | Screen auto-rotation toggle and orientation locks |
| `alias sound = android.settings.SOUND_SETTINGS` | `android.settings.SOUND_SETTINGS` | Volume sliders, ringtones, media levels, and vibration |
| `alias dnd = android.settings.ZEN_MODE_SETTINGS` | `android.settings.ZEN_MODE_SETTINGS` | Do Not Disturb (DND) mode and silence rules |
| `alias dndauto = android.settings.ZEN_MODE_AUTOMATION_SETTINGS` | `android.settings.ZEN_MODE_AUTOMATION_SETTINGS` | DND automation schedules (sleeping, calendar events) |
| `alias dndprio = android.settings.ZEN_MODE_PRIORITY_SETTINGS` | `android.settings.ZEN_MODE_PRIORITY_SETTINGS` | DND priority exceptions (starred callers, repeat calls) |
| `alias notifications = android.settings.NOTIFICATION_SETTINGS` | `android.settings.NOTIFICATION_SETTINGS` | System notification badges, lock screen banners, history |
| `alias appnotifs = android.settings.APP_NOTIFICATION_SETTINGS` | `android.settings.APP_NOTIFICATION_SETTINGS` | Per-app notification channels and alert overrides |
| `alias notifaccess = android.settings.NOTIFICATION_LISTENER_SETTINGS` | `android.settings.NOTIFICATION_LISTENER_SETTINGS` | Notification listener permissions for companion apps |
| `alias dndaccess = android.settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS` | `android.settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS` | Apps permitted to override Do Not Disturb mode |
| `alias mediaout = android.settings.MEDIA_ROUTER_SETTINGS` | `android.settings.MEDIA_ROUTER_SETTINGS` | Audio media routing and wireless speaker output |
| `alias screensaver = android.settings.DREAM_SETTINGS` | `android.settings.DREAM_SETTINGS` | Screen Saver (Daydream) dock and charging options |
| `alias caption = android.settings.CAPTIONING_SETTINGS` | `android.settings.CAPTIONING_SETTINGS` | Live captioning, subtitle typography, and contrast |
| `alias vr = android.settings.VR_LISTENER_SETTINGS` | `android.settings.VR_LISTENER_SETTINGS` | Virtual Reality helper services and listener policies |

---

### Category C: System, Battery, and Privacy

| Alias Command | Target Action | Purpose / Description |
| :--- | :--- | :--- |
| `alias battery = android.settings.BATTERY_SAVER_SETTINGS` | `android.settings.BATTERY_SAVER_SETTINGS` | Battery Saver mode, battery usage charts, and battery health |
| `alias battopt = android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS` | `android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS` | Battery optimization exemptions (prevent app killing) |
| `alias about = android.settings.DEVICE_INFO_SETTINGS` | `android.settings.DEVICE_INFO_SETTINGS` | About Phone (model, Android version, kernel, build number) |
| `alias update = android.settings.SYSTEM_UPDATE_SETTINGS` | `android.settings.SYSTEM_UPDATE_SETTINGS` | Check and download OTA Android system updates |
| `alias storage = android.settings.INTERNAL_STORAGE_SETTINGS` | `android.settings.INTERNAL_STORAGE_SETTINGS` | Internal flash storage breakdown, free space, and clean up |
| `alias sdcard = android.settings.MEMORY_CARD_SETTINGS` | `android.settings.MEMORY_CARD_SETTINGS` | External MicroSD card storage and formatting |
| `alias privacy = android.settings.PRIVACY_SETTINGS` | `android.settings.PRIVACY_SETTINGS` | Privacy dashboard, camera/microphone toggles, ad settings |
| `alias security = android.settings.SECURITY_SETTINGS` | `android.settings.SECURITY_SETTINGS` | Device lock screen, PIN/password, Google Play Protect |
| `alias finger = android.settings.FINGERPRINT_ENROLL` | `android.settings.FINGERPRINT_ENROLL` | Enroll or re-calibrate biometric fingerprints |
| `alias bio = android.settings.BIOMETRIC_ENROLL` | `android.settings.BIOMETRIC_ENROLL` | Unified biometric enrollment (face unlock, fingerprint) |
| `alias location = android.settings.LOCATION_SOURCE_SETTINGS` | `android.settings.LOCATION_SOURCE_SETTINGS` | GPS location toggles, precision, and location permissions |
| `alias accounts = android.settings.SYNC_SETTINGS` | `android.settings.SYNC_SETTINGS` | User accounts, Google account sync, and enterprise profiles |
| `alias addaccount = android.settings.ADD_ACCOUNT_SETTINGS` | `android.settings.ADD_ACCOUNT_SETTINGS` | Wizard to add an email, calendar, or corporate account |
| `alias datetime = android.settings.DATE_SETTINGS` | `android.settings.DATE_SETTINGS` | Date, time, 24-hour format, and automatic network time |
| `alias locale = android.settings.LOCALE_SETTINGS` | `android.settings.LOCALE_SETTINGS` | System display language and regional formats |
| `alias print = android.settings.PRINT_SETTINGS` | `android.settings.PRINT_SETTINGS` | Print services, wireless printers, and print spooler |
| `alias regulatory = android.settings.SHOW_REGULATORY_INFO` | `android.settings.SHOW_REGULATORY_INFO` | Regulatory certification labels and hardware compliance |
| `alias workprofile = android.settings.SHOW_WORK_POLICY_INFO` | `android.settings.SHOW_WORK_POLICY_INFO` | Work profile policies and corporate device management |

---

### Category D: App Management and Permissions

| Alias Command | Target Action | Purpose / Description |
| :--- | :--- | :--- |
| `alias apps = android.settings.APPLICATION_SETTINGS` | `android.settings.APPLICATION_SETTINGS` | Application manager, recent apps, and storage breakdown |
| `alias allapps = android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS` | `android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS` | Manage all apps (including hidden and system applications) |
| `alias manageapps = android.settings.MANAGE_APPLICATIONS_SETTINGS` | `android.settings.MANAGE_APPLICATIONS_SETTINGS` | Installed third-party application list |
| `alias dev = android.settings.APPLICATION_DEVELOPMENT_SETTINGS` | `android.settings.APPLICATION_DEVELOPMENT_SETTINGS` | Developer options (USB debugging, animation scales, ADB) |
| `alias defaults = android.settings.MANAGE_DEFAULT_APPS_SETTINGS` | `android.settings.MANAGE_DEFAULT_APPS_SETTINGS` | Configure default browser, phone, SMS, and assistant apps |
| `alias home = android.settings.HOME_SETTINGS` | `android.settings.HOME_SETTINGS` | Select default home launcher (set Minimal OS as default) |
| `alias unknown = android.settings.MANAGE_UNKNOWN_APP_SOURCES` | `android.settings.MANAGE_UNKNOWN_APP_SOURCES` | Authorize APK sideloading per application |
| `alias overlay = android.settings.MANAGE_OVERLAY_PERMISSION` | `android.settings.MANAGE_OVERLAY_PERMISSION` | Display over other apps (Draw over apps permission) |
| `alias writesettings = android.settings.MANAGE_WRITE_SETTINGS` | `android.settings.MANAGE_WRITE_SETTINGS` | Allow apps to modify system settings |
| `alias allfiles = android.settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` | `android.settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` | Manage All Files Access (MANAGE_EXTERNAL_STORAGE) |
| `alias usage = android.settings.USAGE_ACCESS_SETTINGS` | `android.settings.USAGE_ACCESS_SETTINGS` | App usage stats and Digital Wellbeing screen-time access |
| `alias appops = android.settings.APP_OPS_SETTINGS` | `android.settings.APP_OPS_SETTINGS` | Detailed runtime app operations and permissions |
| `alias bgdata = android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS` | `android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS` | Apps permitted to use cellular data in the background |
| `alias webview = android.settings.WEBVIEW_SETTINGS` | `android.settings.WEBVIEW_SETTINGS` | System WebView provider selection |
| `alias searchsettings = android.settings.SEARCH_SETTINGS` | `android.settings.SEARCH_SETTINGS` | System on-device search index settings |

---

### Category E: Accessibility and Input Devices

| Alias Command | Target Action | Purpose / Description |
| :--- | :--- | :--- |
| `alias a11y = android.settings.ACCESSIBILITY_SETTINGS` | `android.settings.ACCESSIBILITY_SETTINGS` | Screen readers, magnification, display contrast, and switches |
| `alias keyboard = android.settings.INPUT_METHOD_SETTINGS` | `android.settings.INPUT_METHOD_SETTINGS` | Virtual keyboard list and active input method settings |
| `alias hardkeyboard = android.settings.HARD_KEYBOARD_SETTINGS` | `android.settings.HARD_KEYBOARD_SETTINGS` | Physical USB/Bluetooth keyboard layout and shortcuts |
| `alias kblayout = android.settings.INPUT_METHOD_SUBTYPE_SETTINGS` | `android.settings.INPUT_METHOD_SUBTYPE_SETTINGS` | Secondary keyboard layouts and multilingual inputs |
| `alias dict = android.settings.USER_DICTIONARY_SETTINGS` | `android.settings.USER_DICTIONARY_SETTINGS` | User personal dictionary and text replacement shortcuts |
| `alias voiceinput = android.settings.VOICE_INPUT_SETTINGS` | `android.settings.VOICE_INPUT_SETTINGS` | Voice typing and speech-to-text engine selection |
| `alias tts = android.settings.TTS_SETTINGS` | `android.settings.TTS_SETTINGS` | Text-to-Speech voice pitch, speed, and language data |
| `alias captions = android.settings.CAPTIONING_SETTINGS` | `android.settings.CAPTIONING_SETTINGS` | System closed-caption font styles and subtitles |
| `alias quicklaunch = android.settings.QUICK_LAUNCH_SETTINGS` | `android.settings.QUICK_LAUNCH_SETTINGS` | Physical keyboard shortcut key configurations |
| `alias settings = android.settings.SETTINGS` | `android.settings.SETTINGS` | Top-level native Android Settings app |

---

### Category F: Web Search and Query Engines

*Note*: If you provide a search query after the alias in the command bar (e.g. `g kotlin coroutines`), `{query}` is automatically replaced with your search query.

| Alias Command | Target URL Template | Service / Purpose |
| :--- | :--- | :--- |
| `alias g = https://www.google.com/search?q={query}` | Google Search | Fast web search |
| `alias ddg = https://duckduckgo.com/?q={query}` | DuckDuckGo Search | Privacy-focused web search |
| `alias br = https://search.brave.com/search?q={query}` | Brave Search | Independent web search |
| `alias kagi = https://kagi.com/search?q={query}` | Kagi Search | Ad-free premium web search |
| `alias bing = https://www.bing.com/search?q={query}` | Microsoft Bing | Bing web search |
| `alias gh = https://github.com/search?q={query}` | GitHub Search | Search open source code and repositories |
| `alias ghr = https://github.com/{query}` | GitHub Direct Repo | Navigate directly to `github.com/user/repo` |
| `alias yts = https://www.youtube.com/results?search_query={query}` | YouTube Search | Search YouTube video catalog |
| `alias rd = https://www.reddit.com/search/?q={query}` | Reddit Global Search | Search discussions across all subreddits |
| `alias r = https://www.reddit.com/r/{query}` | Direct Subreddit | Jump directly to `reddit.com/r/subreddit` |
| `alias wiki = https://en.wikipedia.org/wiki/Special:Search?search={query}` | Wikipedia Search | Global encyclopedia articles |
| `alias so = https://stackoverflow.com/search?q={query}` | StackOverflow | Developer programming questions and answers |
| `alias maps = https://www.google.com/maps/search/{query}` | Google Maps | Places, directions, and navigation search |
| `alias osm = https://www.openstreetmap.org/search?query={query}` | OpenStreetMap | Open geographic map search |
| `alias tr = https://translate.google.com/?text={query}` | Google Translate | Translate text query |
| `alias wolf = https://www.wolframalpha.com/input?i={query}` | WolframAlpha | Computational knowledge and math solver |
| `alias perplex = https://www.perplexity.ai/search?q={query}` | Perplexity AI | AI-powered conversational search |
| `alias chatgpt = https://chatgpt.com/?q={query}` | ChatGPT | Direct query to ChatGPT |
| `alias claude = https://claude.ai/` | Claude AI | Launch Claude AI assistant |
| `alias gemini = https://gemini.google.com/` | Google Gemini | Launch Google Gemini assistant |
| `alias npm = https://www.npmjs.com/search?q={query}` | NPM Registry | Node.js JavaScript packages |
| `alias pypi = https://pypi.org/search/?q={query}` | Python PyPI | Python packages and libraries |
| `alias crates = https://crates.io/search?q={query}` | Rust Crates | Rust package repository |
| `alias arxiv = https://arxiv.org/search/?query={query}` | arXiv Archive | Scientific research papers and preprints |
| `alias pubmed = https://pubmed.ncbi.nlm.nih.gov/?term={query}` | PubMed | Biomedical and life sciences literature |

---

### Category G: App Launch and Package Shortcuts

*Note*: Launching apps by package ID works on every device even if the app's localized display title changes.

| Alias Command | Target Package ID / App | Description |
| :--- | :--- | :--- |
| `alias termux = com.termux` | `com.termux` | Termux Terminal Emulator & Linux environment |
| `alias fdroid = org.fdroid.fdroid` | `org.fdroid.fdroid` | F-Droid Open Source App Repository |
| `alias aurora = com.aurora.store` | `com.aurora.store` | Aurora Store (Google Play Store client) |
| `alias vlc = org.videolan.vlc` | `org.videolan.vlc` | VLC Media Player |
| `alias spotify = com.spotify.music` | `com.spotify.music` | Spotify Music Streaming |
| `alias obs = md.obsidian` | `md.obsidian` | Obsidian Markdown Knowledge Base |
| `alias bit = com.x8bit.bitwarden` | `com.x8bit.bitwarden` | Bitwarden Password Manager |
| `alias keepass = keepass2android.keepass2android` | `keepass2android.keepass2android` | KeePass2Android Offline Password Safe |
| `alias signal = org.thoughtcrime.securesms` | `org.thoughtcrime.securesms` | Signal Private Messenger |
| `alias tg = org.telegram.messenger` | `org.telegram.messenger` | Telegram Messenger |
| `alias wa = com.whatsapp` | `com.whatsapp` | WhatsApp Messenger |
| `alias discord = com.discord` | `com.discord` | Discord Chat & Voice |
| `alias slack = com.Slack` | `com.Slack` | Slack Work Workspace |
| `alias newpipe = org.schabi.newpipe` | `org.schabi.newpipe` | NewPipe Lightweight YouTube Client |
| `alias play = com.android.vending` | `com.android.vending` | Google Play Store |

---

### Category H: Minimal OS Command Macros

Minimal OS allows you to alias compound terminal commands and natural-language actions into simple single-word triggers.

| Alias Command | Target Launcher Command | What It Does When Run |
| :--- | :--- | :--- |
| `alias pomo25 = pomo 25` | `pomo 25` | Starts a 25-minute Pomodoro focus session immediately |
| `alias pomo50 = pomo 50` | `pomo 50` | Starts a 50-minute deep-work Pomodoro session |
| `alias pomo15 = pomo 15` | `pomo 15` | Starts a quick 15-minute sprint session |
| `alias break5 = timer 5m` | `timer 5m` | Sets a 5-minute countdown break timer |
| `alias break10 = timer 10m` | `timer 10m` | Sets a 10-minute countdown break timer |
| `alias morning = alarm 07:00` | `alarm 07:00` | Sets an alarm for 7:00 AM |
| `alias standup = event 09:30 Daily Standup` | `event 09:30 Daily Standup` | Creates a 9:30 AM calendar event |
| `alias buy = - buy` | `- buy` | Shorthand to append items to your shopping to-do list |
| `alias todo = -` | `-` | Maps `todo <task>` to native to-do item creation |
| `alias task = -` | `-` | Maps `task <item>` to native to-do item creation |
| `alias note = !` | `!` | Maps `note <thought>` to Minimal Memo note creation |
| `alias memo = !` | `!` | Maps `memo <thought>` to Minimal Memo note creation |
| `alias rm = uninstall` | `uninstall` | Shorthand for app uninstaller (e.g. `rm chatgpt`) |
| `alias fav = pin` | `pin` | Shorthand to pin an app shortcut to Home screen (e.g. `fav spotify`) |
| `alias ai = assistant` | `assistant` | Routes queries directly to default AI assistant |
| `alias yt = yt` | `yt` | Shorthand YouTube search launcher (e.g. `yt veritasium`) |

---

## 6. Troubleshooting and OEM Compatibility

### 1. OEM Settings Variations
While Android's `android.settings.*` intents are standard across AOSP, some vendor skins (such as Samsung One UI, Xiaomi HyperOS/MIUI, or Vivo OriginOS) may redirect certain secondary sub-settings or restrict them:
- **Universal Intents**: `WIFI_SETTINGS`, `BLUETOOTH_SETTINGS`, `AIRPLANE_MODE_SETTINGS`, `DISPLAY_SETTINGS`, `SOUND_SETTINGS`, `APPLICATION_SETTINGS`, `APPLICATION_DEVELOPMENT_SETTINGS`, and `DATE_SETTINGS` function reliably on virtually all devices.
- If a manufacturer-specific intent fails, Minimal OS will safely catch the exception and display a non-blocking toast: `Settings action failed: <reason>`.

### 2. URL Encodings with Special Characters
When defining URL templates, always enclose parameters in `{query}`. Minimal OS handles URL encoding (e.g., spaces converting to `%20` or `+`) automatically so complex search strings do not break your browser intent.

### 3. Space Separation in Command Line
When creating an alias from the command bar, ensure there are spaces around the equals sign:
- Correct: `alias wifi = android.settings.WIFI_SETTINGS`
- Incorrect: `alias wifi=android.settings.WIFI_SETTINGS`

### 4. Overwriting an Existing Alias
Running `alias <name> = <new_target>` with an existing name will instantly overwrite the previous target with the new definition.

---

*Minimal OS Launcher — Fast, Intentional, Local.*
