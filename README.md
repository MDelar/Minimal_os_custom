# Minimal OS Custom

My personal fork of [Minimal OS](https://github.com/zaryif/Minimal_os), a text-first Android launcher built around a command bar. I've tuned it for the **Unihertz Titan 2 Elite** and other phones with a physical keyboard.

## What's different in this fork

- **Omarchy themes:** 18 extra dark themes (Tokyo Night, Nord, Lumon, Gruvbox and more) under Settings › Theme
- **Type anywhere:** on the home screen, start typing on the keyboard and the command bar opens with what you typed
- **Home key:** the keyboard's Home key and the home gesture always return to the home screen
- **Wallpaper sync:** Android's wallpaper is kept matching the launcher, so nothing flashes when switching apps
- **Fixes:** notification taps in the Hub, onboarding on square screens, focus when returning home, and relative times like "in 30 mins"

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

Based on [Minimal OS](https://github.com/zaryif/Minimal_os) by MD. Zarif Azfar. Theme colours are from [Omarchy](https://github.com/omacom/omarchy). Both are MIT-licensed. See [THIRD_PARTY.md](THIRD_PARTY.md).

This fork is MIT-licensed too. See [LICENSE](LICENSE).
