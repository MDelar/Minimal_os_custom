# Minimal OS Release Notes - v1.9.4 (Build 25)

## Google Play Console Release Notes (en-US)

Paste this exact snippet into the Release Notes field in Google Play Console (en-US):

```text
<en-US>
Minimal OS v1.9.4:
- Enhanced Natural Language Event & Alarm Parser: Resolved edge case where quantities/numbers in event titles were mistakenly parsed as hours; fixed calendar year rollover and wake-up alarm PM conversion.
- Exact App Uninstallation: Uninstall suggestions now dispatch by unique package identifier, guaranteeing 100% precision with zero ambiguity.
- Expanded Custom Aliases: Added support for Pomodoro (pomo), YouTube (yt), WhatsApp (wa), and uninstall targets within custom command aliases.
- Phone & USSD Dialing Reliability: Sanitized tel intent URI encoding to preserve hashes (#) and asterisks (*) for carrier USSD codes.
- Architectural Hardening: Eliminated obsolete dead code routines and introduced a robust automated unit testing suite.
- 100% offline, zero-telemetry architecture with zero ads and zero data tracking.
</en-US>
```

---

## Detailed GitHub Release Notes (v1.9.4)

### What's New in v1.9.4

#### 1. Intelligent Natural Date/Time Parser Overhaul
- **Fixed Bare Number Stripping**: Titles like `"Meeting with 2 clients"` or `"Buy 3 laptops"` previously had numbers mistakenly stripped as explicit times (e.g. 2:00 PM). The parser now requires explicit time indicators (`at <hour>`, colon/dot minutes like `7:30`, or `am/pm`).
- **Calendar Year Calculation Fix**: Fixed millisecond comparison bug when scheduling same-day events (e.g. `"Oct 30 at 8pm"`) where events were incorrectly scheduled for the following year.
- **Morning Alarm Normalization**: Fixed alarm inputs like `alarm 7:00` being forcibly converted to 7:00 PM (19:00). Wake-up alarms without explicit meridian suffixes now accurately default to morning times.

#### 2. Exact Package Uninstallation
- Interactive app uninstallation suggestion chips now pass the app's exact package identifier to `handleUninstall`, preventing potential fuzzy label mismatch when apps share identical or prefix names.

#### 3. Expanded Custom Command Aliases
- Custom aliases can now seamlessly expand into built-in `pomo`, `pomodoro`, `yt`, `youtube`, `uninstall`, `wa`, and `whatsapp` actions (e.g. `alias focus = pomo 25m`).

#### 4. USSD & Phone Dialing Fix
- Properly encoded telephone URIs (`Uri.encode(phoneNumber)`) so that USSD carrier codes containing `#` (e.g. `*121#`) are dialed correctly without being truncated as URI fragments.

#### 5. Code Quality & Unit Tests
- Removed 88 lines of legacy, uncalled parsing functions in `ExecuteCommandUseCase`.
- Added JUnit 4 test runner and 11 unit tests covering `NaturalDateTimeParser` and `CommandParser`.
- Verified 100% test passing and debug APK compilation.
