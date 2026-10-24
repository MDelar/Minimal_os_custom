# Google Play Closed Testing & Production Release Guide

## 1. The 14-Day / 20-Tester Policy Explained

Since November 13, 2023, Google requires all personal developer accounts to meet specific testing requirements before they can publish an app to Production:

- Minimum 20 testers must be opted in to your closed test.
- Testers must remain opted in for at least 14 days continuously.
- Once 14 continuous days are completed, an "Apply for production" button activates on your Google Play Console Dashboard.

---

## 2. Testing Tracks Overview: Internal vs Closed vs Open

| Track | Max Testers | Review Required? | Contributes to 14-Day Production Gate? |
|---|---|---|---|
| **Internal Testing** | Up to 100 testers | No review (Instant availability) | No (Used for immediate testing and quick bugfixes) |
| **Closed Testing** | Unlimited (via list or Google Group) | Fast Google policy check | **YES (Required for 14 days)** |
| **Open Testing** | Publicly discoverable | Full Google Play review | No (Unlocked alongside or after closed testing) |

---

## 3. How to Set Up Closed Testing (Step-by-Step)

### Step 1: Create or Open Closed Testing Track
1. Navigate to Google Play Console: https://play.google.com/console
2. Select your app: **Minimal OS** (App ID: `4976361961120095801`)
3. In the left menu under **Testing**, click **Closed testing**.
4. Click **Manage track** on your closed testing track (e.g., Alpha).

### Step 2: Set Up Testers
1. Click the **Testers** tab.
2. Select **Email lists** or **Google Groups**.
   - *Recommended method: Create a Google Group (e.g. `minimal-os-testers@googlegroups.com`). Anyone who joins the Google Group automatically gets test access without you manually typing 20 individual emails.*
3. Add at least 20 tester emails (or paste your Google Group email).
4. Click **Save changes** at the bottom right.

### Step 3: Copy Your Opt-In Shareable Links
Under the **Testers** tab at the bottom, find the section **How testers join your test**:
- **Join on the web**: `https://play.google.com/apps/testing/com.minimal.os`
- **Join on Android**: Link to download directly via Google Play Store on device.

*Note: Testers MUST click the web link first and press "Become a tester". Only then can they download the app via Google Play.*

### Step 4: Create a Release in Closed Testing
1. In the Closed testing track, click **Create new release**.
2. Upload the signed `.aab` file: `Minimal_OS_v1.9.3_b24.aab` (located on your Desktop at `Minimal_OS_Store_Listing/Minimal_OS_v1.9.3_b24.aab` or in `store_listing/Minimal_OS_v1.9.3_b24.aab`).
3. Set Release Name: `1.9.3` (Build 24).
4. Paste the Release Notes from `store_listing/copy/06_release_notes.md`.
5. Click **Next**, review errors/warnings, and click **Save and send for review**.

---

## 4. Ready-to-Send Tester Recruitment Message

Use this clean message when inviting testers via WhatsApp, Telegram, Discord, Reddit (r/AndroidClosedTesting), or Email:

```text
Subject: Beta Testing Invitation: Minimal OS Launcher

Hi everyone,

I am testing Minimal OS, a distraction-free, privacy-first Android launcher featuring a built-in terminal command line and zero notification clutter.

To help us release to Google Play Production, we need 20 testers to keep the app installed for 14 days.

Step 1: Join the Google Group or accept the tester invite:
[Insert your Google Group or Opt-In Web URL: https://play.google.com/apps/testing/com.minimal.os]

Step 2: Download Minimal OS from Google Play via the Android link.

Step 3: Keep the app on your phone and share any feedback!

Thank you for supporting independent open-source software!
```

---

## 5. Applying for Production Access (After Day 14)

Once 20 testers have completed 14 days:
1. Go to the Play Console **Dashboard**.
2. Under "Apply for production", click **Apply for production**.
3. Answer the brief Google feedback questionnaire:
   - How did you recruit testers? (e.g., Community forums, friends, family, open-source contributors).
   - What feedback did you receive? (e.g., Testers appreciated the fast AMOLED typography and terminal commands; fixed minor padding and gesture responsiveness).
   - What changes did you make based on feedback? (e.g., Improved command help dialog, added timer presets).
4. Submit the request. Google typically approves production access within 2 to 5 business days.
