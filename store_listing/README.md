# Minimal OS - Google Play Store Listing & Publishing Kit

This directory contains all official top-brand graphic assets, showcase mockups, real device captures, signed release binaries, and text copy required to publish Minimal OS to Google Play Console (Closed Testing, Open Testing, and Production).

---

## Directory Structure

```text
store_listing/
├── README.md                                  # This directory index and upload guide
├── Minimal_OS_v1.9.3_b24.aab                  # Signed Release App Bundle for Google Play Console (v1.9.3, Build 24)
├── Minimal_OS_v1.9.3_b24.apk                  # Signed Release APK for direct sideloading and testing
├── copy/                                      # Ready-to-paste store listing and policy text
│   ├── 01_store_listing_copy.md               # Title, short description (<80 chars), full description
│   ├── 02_privacy_policy.md                   # Zero-telemetry privacy policy
│   ├── 03_data_safety_answers.md              # Exact Play Console Data Safety questionnaire answers
│   ├── 04_target_audience_and_content_rating.md # Content rating and app declarations walkthrough
│   ├── 05_closed_testing_and_production_guide.md # 14-day / 20-tester playbook to unlock production
│   └── 06_release_notes.md                    # Release notes for v1.9.3 (Build 24) in en-US
├── graphics/                                  # Top-Brand Formatted Store Assets
│   ├── icon_512x512.png                       # 512x512 PNG app icon (Main store listing)
│   ├── feature_graphic_1024x500.png           # 1024x500 PNG banner (Main store listing)
│   ├── screenshots/                           # 8 Curated Flagship Showcase Mockup Slides (1080x2400)
│   │   ├── 01_live_home_screen.png            # 01 // Pure Focus Launcher - Type less. Do more.
│   │   ├── 02_command_terminal_buffer.png     # 02 // Terminal Command Bar - Interactive chat buffer
│   │   ├── 03_live_focus_timer.png            # 03 // Deep Focus Sessions - Live countdown timer
│   │   ├── 04_apple_grade_onboarding.png      # 04 // First-Class Experience - Apple-grade onboarding
│   │   ├── 05_alphabetical_app_drawer.png     # 05 // Distraction-Free Apps - Alphabetic text drawer
│   │   ├── 06_widgets_deck.png                # 06 // Unified Dashboard - All-in-one widgets deck
│   │   ├── 07_quick_memo_tasks.png            # 07 // Instant Capture - Slide-up scratchpad
│   │   └── 08_zero_telemetry_privacy.png      # 08 // Privacy By Architecture - 100% private & offline
│   ├── tablet_7inch/                          # 4 x 7-Inch Tablet Showcase Graphics (1200x1920)
│   │   ├── 01_tablet_7inch.png
│   │   ├── 02_tablet_7inch.png
│   │   ├── 03_tablet_7inch.png
│   │   └── 04_tablet_7inch.png
│   └── tablet_10inch/                         # 4 x 10-Inch Tablet Showcase Graphics (1600x2560)
│       ├── 01_tablet_10inch.png
│       ├── 02_tablet_10inch.png
│       ├── 03_tablet_10inch.png
│       └── 04_tablet_10inch.png
├── video/                                     # High-definition promotional and showcase videos
│   ├── minimal_os_marketing_vertical_9x16.mp4 # 9:16 Vertical app overview (Shorts, Reels, Play Store)
│   ├── minimal_os_marketing_landscape_16x9.mp4 # 16:9 Landscape app overview (YouTube, Play Store)
│   ├── minimal_os_command_showcase_vertical_9x16.mp4 # 9:16 Vertical Command Bar & Alias deep dive
│   └── minimal_os_command_showcase_landscape_16x9.mp4 # 16:9 Landscape Command Bar & Alias deep dive
├── generate_top_brand_store_graphics.py       # Script to generate phone and tablet showcase graphics
├── generate_marketing_video.py                # Automated marketing overview video generator script
├── generate_command_showcase_video.py         # Command Bar typing & execution showcase video generator script
└── screenshots_real/                          # Complete raw captures archive from running device
```

---

## Desktop Mirror

All assets in this directory are synchronized with:
`/Users/zaryif/Desktop/Minimal_OS_Store_Listing/`

---

## Google Play Console Step-by-Step Upload Checklist

### 1. Store Presence > Main Store Listing
- **App icon**: Upload `graphics/icon_512x512.png`
- **Feature graphic**: Upload `graphics/feature_graphic_1024x500.png`
- **Phone screenshots**: Upload all 8 slides from `graphics/screenshots/`
- **7-inch tablet screenshots**: Upload `graphics/tablet_7inch/`
- **10-inch tablet screenshots**: Upload `graphics/tablet_10inch/`
- **Short description**: Copy from `copy/01_store_listing_copy.md`
- **Full description**: Copy from `copy/01_store_listing_copy.md`

### 2. Testing > Closed Testing (or Open Testing / Production)
- **Upload Release Bundle**: `Minimal_OS_v1.9.3_b24.aab`
- **Release Name**: `1.9.3`
- **Release Notes**: Copy the `<en-US>` block from `copy/06_release_notes.md`

---

## Shareable Links Reference

- **Internal Testing Link**:
  `https://play.google.com/apps/internaltest/4976361961120095801`

- **Closed Testing Web Opt-In Link**:
  `https://play.google.com/apps/testing/com.minimal.os`

- **Play Store Web Link (After Production Approval)**:
  `https://play.google.com/store/apps/details?id=com.minimal.os`
