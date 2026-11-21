# Minimal OS Custom

My personal fork of [Minimal OS](https://github.com/zaryif/Minimal_os), a text-first Android launcher built around a command bar. I've tuned it for the **Unihertz Titan 2 Elite** and other phones with a physical keyboard.

## What's different in this fork

### Look
- **Omarchy themes:** 18 extra dark themes (Tokyo Night, Nord, Lumon, Gruvbox and more) under Settings › Theme
- **Your own header:** the top-left of home shows a shell-style prompt, `mdelar@titan:~$` by default. Change it in Settings › Appearance › Home Header
- **Unlock animation:** unlocking onto home types your header out, prints a greeting and wipes in with a scan line in the theme colour. About half a second, tap to skip, and it can be turned off in Settings › Appearance

### Widgets
- **Weather:** current conditions, today's high and low, and a forecast for any town you set. Recolours with the theme. Data from [Open-Meteo](https://open-meteo.com), no account needed
- **Calendar:** your next three events as a text list (`14:00  maths 10A`, `tmrw 08:50  form time`). Tap one to open it, or long-press the card for diagnostics if events are missing

### App drawer
- **Notification counts:** apps with notifications show a small count in the theme colour
- **Notified apps first:** optionally list those apps above the A–Z list (Settings › System › Notified Apps First)

### Keyboard phones
- **Type anywhere:** on the home screen, start typing on the keyboard and the command bar opens with what you typed
- **Home key:** the keyboard's Home key and the home gesture always return to the home screen

### Fixes
- Notification taps in the Hub open the app on Android 14 and later
- Onboarding and Replay Setup Wizard fit square screens
- Android's wallpaper is kept matching the launcher, so nothing flashes when switching apps
- The command bar no longer grabs focus when you return home
- Relative times like "in 30 mins" are worked out correctly late at night

Everything else comes from the original project, which I merge in when it's updated.

## Commands

Type in the command bar on the home screen:

| Type | Does |
|---|---|
| `@name message` | Text someone |
| `#name` | Call someone |
| `-thing to do` | Add a task |
| `!note` | Save a note |
| `*event tomorrow at 3pm` | Add a calendar event |
| `+15m` | Start a timer |
| `:7:00am` | Set an alarm |
| `?question` | Ask your AI assistant |
| `pinit text` | Pin a note to the notification bar |
| an app name | Open the app |
| anything else | Search the web |

[ALIASES_AND_SHORTCUTS.md](ALIASES_AND_SHORTCUTS.md) has more aliases and shortcuts from the original project.

## Building

Needs Android Studio or the Android SDK, and JDK 17.

```
./gradlew assembleDebug
```

Release builds are signed with my own key, which isn't in this repo. Put your own in `keystore.properties` (see `app/build.gradle.kts`) to sign one yourself.

Android 8.0 or newer. Kotlin and Jetpack Compose.

## Credits and licence

Based on [Minimal OS](https://github.com/zaryif/Minimal_os) by MD. Zarif Azfar. Theme colours are from [Omarchy](https://github.com/omacom/omarchy). Both are MIT-licensed. Weather data is from [Open-Meteo](https://open-meteo.com) (CC BY 4.0). See [THIRD_PARTY.md](THIRD_PARTY.md).

This fork is MIT-licensed too. See [LICENSE](LICENSE).
