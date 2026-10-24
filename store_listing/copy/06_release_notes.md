# Minimal OS Release Notes - v1.9.3 (Build 24)

## Google Play Console Release Notes (en-US)

Paste this exact snippet into the Release Notes field in Google Play Console (en-US):

```text
<en-US>
Minimal OS v1.9.3:
- Live Alarm Widget Sync: Fixed alarm widget false-positive active states; now dynamically queries real system alarms and displays clear standby state when no alarm is set.
- Clock App Integration: Quick one-tap access to device clock app and active alarm management directly from the widget deck.
- Settings System Widgets Visibility: Replaced overflowing single-row chips with flexible wrapping FlowRows so unadded or deleted system widgets never disappear off-screen.
- Robust Settings Search: Added bidirectional category filtering to seamlessly match queries like "widgets", "shortcuts", and "themes".
- Automatic System Widget Layout Registration: Adding hosted Android widgets automatically registers them into Home and Deck layouts.
- 100% offline, zero-telemetry architecture with zero ads and zero data tracking.
</en-US>
```

---

## Detailed GitHub Release Notes (v1.9.3)

### What's New in v1.9.3

#### 1. Live System Alarm Sync & Widget Overhaul
- **Resolved False-Positive Presetting**: Fixed the alarm widget card defaulting to "7:00 AM" and "Rings Tomorrow" when no alarm was active.
- **Real-Time System Querying**: The alarm card now queries Android's `AlarmManager.getNextAlarmClock()` on launch and updates dynamically.
- **Clear Standby Indication**: When no alarm is active, the card displays `--:--` and `"No alarm set • Choose a preset or tap +"`, ensuring zero ambiguity.
- **Interactive Quick Presets & Clock App Link**: Selecting presets now schedules live alarms seamlessly with confirmation feedback, and an "OPEN CLOCK" button provides immediate access to system alarms and timers.

#### 2. Settings System Widgets Visibility & Flow Layout
- **No More Hidden or Clipped Widgets**: Fixed an issue where unadded or deleted home screen system widgets disappeared because of an unscrollable single `Row`. Replaced with responsive `FlowRow` wrapping so all widgets (`+ clock`, `+ calendar`, `+ battery`, `+ alarms`, `+ widgets`) are always visible and accessible.
- **Smart Category Search**: Resolved query mismatch where searching for "widgets" hid the widgets section due to strict singular string checks. Added bidirectional keyword matching for plural searches.
- **Persistent Layout Registration**: Automatically ensures `SYSTEM_WIDGETS` is appended to active layout orders whenever a system app widget is added.

#### 3. Architecture & Quality
- Target SDK: Android 15/16 (API 36).
- Minimum SDK: Android 8.0 (API 26).
- 100% private, local-first, zero telemetry, zero trackers, and zero ads.
