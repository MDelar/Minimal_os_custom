# Minimal OS Release Notes - v1.9.5 (Build 26)

## Google Play Console Release Notes (en-US)

Paste this exact snippet into the Release Notes field in Google Play Console (en-US):

```text
<en-US>
Minimal OS v1.9.5:
- 3rd-Party App Uninstallation Fix: Added mandatory REQUEST_DELETE_PACKAGES permission and resilient multi-tier fallback intents to guarantee native confirmation dialogs appear reliably on Android 8 through 16.
- Direct Home Screen Pinning: Added `pin <app>` and `unpin <app>` commands with instant fuzzy app matching, dedicated PIN badges, and automatic shortcuts deck activation.
- App Drawer Power Actions: Long-press any app in the app drawer to Pin/Unpin, Uninstall, or inspect system App Info, with visual PINNED indicator chips.
- Fluid Physics & Motion Polish: Added Apple/Android physics spring transitions for screen navigation, buttery Command Bar suggestion dropdowns, tactile card/button press physics, and dynamic content morphing.
- 100% offline, zero-telemetry architecture with zero ads and zero data tracking.
</en-US>
```

---

## Detailed GitHub Release Notes (v1.9.5)

### What's New in v1.9.5

#### 1. 3rd-Party App Uninstallation Permission & Resilient Intent Fallback
- Declared `android.permission.REQUEST_DELETE_PACKAGES` in `AndroidManifest.xml`. On Android 8.0+ through API 36, Android's `PackageInstaller` automatically rejects package deletion intents without this permission.
- Upgraded `handleUninstall` to use standard `Uri.fromParts("package", packageName, null)` with multi-tier fallback (`Intent.ACTION_DELETE` -> `Intent.ACTION_UNINSTALL_PACKAGE` -> `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).
- Added dynamic lifecycle refresh on `ON_RESUME` so uninstalled apps are immediately pruned from the home screen and pinned shortcuts.

#### 2. Native Command Bar Pinning (`pin <app>` & `unpin <app>`)
- Added `pin <app>`, `fav <app>`, `unpin <app>`, and `unfav <app>` to the Command Bar parser.
- Typing `pin` shows real-time installed app suggestions with a dedicated `PIN` badge; tapping any suggestion directly pins the app to Home.
- Automatically activates the Home Screen `"SHORTCUTS"` deck if not already enabled so newly pinned shortcuts are immediately visible.

#### 3. App Drawer Context Menu
- Long-pressing any app in the App Drawer opens a clean options sheet with Pin/Unpin from Home, Uninstall App, and System App Info.
- Pinned applications now display a subtle `PINNED` badge in the App Drawer.

#### 4. UI Animation & Tactile Motion Polish (/ui-animation)
- **Directional Physics Screen Transitions**: Replaced basic crossfade with spatial `AnimatedContent` spring transitions (App Drawer slides up from bottom, Widgets Deck slides from left, Settings slides from right, Hub slides from top).
- **Fluid Typing Mode Transition**: Smooth transition between Home clock/widgets and Terminal history buffer without layout jumping.
- **Spring Suggestion Dropdowns**: Command Bar contact and app suggestion cards expand smoothly with spring physics and asymmetric exit timing.
- **Tactile Touch Feedback**: Added physical press-down scale compression (`0.97x` / `0.98x`) to buttons, cards, app drawer items, and pinned shortcut chips.
- **Fluid Content Morphing**: Added `animateContentSize` to expandable Todos and Notes widgets.
