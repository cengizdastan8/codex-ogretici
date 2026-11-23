# Ham araştırma raporları (5 ajan)

Sentezin dayandığı ham bulgular. Okunamayan veya yalnız snippet ile doğrulanan kaynaklar raporlarda ayrıca işaretlidir.

## Resmi Android belgeleri (teknik)
ANDROID PLATFORM RESEARCH (as of 2026-11-23). Official sources unless marked SECONDARY or UNVERIFIED.

SOURCE LEGEND
S1 https://developer.android.com/reference/android/view/WindowManager.LayoutParams
S2 https://developer.android.com/reference/android/Manifest.permission
S3 https://developer.android.com/topic/security/risks/tapjacking
S4 https://developer.android.com/reference/android/provider/Settings
S5 https://developer.android.com/preview/privacy/permissions
S6 https://medium.com/androiddevelopers/untrusted-touch-events-2c0e0b9c374c (search excerpt only, fetch returned 403)
S7 https://developer.android.com/about/versions/14/changes/fgs-types-required
S8 https://developer.android.com/develop/background-work/services/fgs/service-types
S9 https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
S10 https://developer.android.com/about/versions/15/behavior-changes-15
S11 https://developer.android.com/develop/ui/views/notifications/notification-permission
S12 https://developer.android.com/about/versions/13/behavior-changes-13
S13 https://developer.android.com/about/versions/16/behavior-changes-all
S14 https://developer.android.com/about/versions/16/behavior-changes-16
S15 https://developer.android.com/blog/posts/android-17-is-here
S16 https://developer.android.com/about/versions/17/behavior-changes-17
S17 https://developer.android.com/developer-verification/guides
S18 https://developer.android.com/google/play/requirements/target-sdk
S19 https://developer.android.com/develop/ui/views/quicksettings-tiles
S20 https://developer.android.com/reference/android/service/quicksettings/TileService
S21 https://developer.android.com/reference/android/provider/Settings.System
S22 https://support.google.com/accessibility/android/answer/11183305?hl=en
S23 https://www.androidauthority.com/android-15-even-dimmer-3436221 (SECONDARY, search excerpt)
S24 https://9to5google.com/2021/02/19/android-12-reduce-bright-colors/ (SECONDARY, search excerpt)
S25 https://developer.android.com/reference/android/provider/Settings.Secure
S26 https://android.googlesource.com/platform/packages/apps/Settings/+/879babf246/res/xml/reduce_bright_colors_settings.xml
S27 https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/b6697b2a32d6e77eaa1786dd850b27e5a04e8ae9 (AOSP-derived mirror, search excerpt, not upstream)
S28 https://developer.android.com/guide/topics/ui/accessibility/service
S29 https://developer.android.com/guide/topics/ui/accessibility/views/service
S30 https://codelabs.developers.google.com/codelabs/developing-android-a11y-service (search excerpt)
S31 https://developer.android.com/about/versions/13/behavior-changes-all (no restricted-settings text found)
S32 https://www.androidauthority.com/android-13-sideloading-apps-restrictions-3161162 (SECONDARY)
S33 https://cleanbrowsing.org/support/mobile/disable-restricted-settings-android (SECONDARY)
S34 https://www.kaspersky.com/blog/android-restricted-settings/49991/ (SECONDARY)
S35 https://www.androidauthority.com/android-15-restricted-settings-sideloading-3481098 (SECONDARY)
S36 https://developer.android.com/build/releases/gradle-plugin
S37 https://developer.android.com/build/releases/agp-9-6-0-release-notes
S38 https://gradle.org/releases/
S39 https://kotlinlang.org/docs/releases.html
S40 https://developer.android.com/jetpack/androidx/releases/core
S41 https://developer.android.com/jetpack/androidx/releases/activity
S42 https://developer.android.com/tools/releases/platforms (cached copy, stale: no API 37 entry)

BOTTOM LINE
- Touch-through overlay works on Android 12+ only while overlay alpha <= 0.8 (single overlay). Above 0.8 the taps are dropped for the app underneath. Full black therefore needs a touchable overlay, which blocks the game/Instagram below.
- Since Android 11, ACTION_MANAGE_OVERLAY_PERMISSION cannot open the app's own toggle. The user lands on the top-level "display over other apps" list.
- No documented API sets brightness below the hardware minimum. The dark overlay is the only documented route.
- A normal app cannot write Reduce bright colors / Extra dim settings.

1. DRAWING OVER OTHER APPS
Facts:
- TYPE_APPLICATION_OVERLAY: added API 26; requires SYSTEM_ALERT_WINDOW; drawn above activities, below status bar/IME; system may move, resize or hide it at any time [S1].
- SYSTEM_ALERT_WINDOW: "Allows an app to create windows using TYPE_APPLICATION_OVERLAY, shown on top of all other apps" [S2]. From Android 6, the user must grant it [S3].
- Settings.canDrawOverlays(context): "Checks if the specified context can draw on top of other apps" [S4].
- ACTION_MANAGE_OVERLAY_PERMISSION: added API 23. The "package:<name>" data URI was honored only before Android R (API 30) [S4].
- From Android 11 the intent "always bring[s] the user to the top-level Settings screen"; any package: data is ignored; the user must select the app [S5].
- FLAG_NOT_TOUCHABLE: window never receives touches; touches go to windows below [S1].
- FLAG_NOT_FOCUSABLE: window gets no key input; it also enables FLAG_NOT_TOUCH_MODAL [S1].
- Android 12+ touch rule for FLAG_NOT_TOUCHABLE windows: touches through the window are delivered only if one holds: same UID; trusted window (accessibility, IME, assistant; TYPE_APPLICATION_OVERLAY is NOT trusted); invisible; alpha 0; a single TYPE_APPLICATION_OVERLAY from this UID with alpha <= maximum obscuring opacity 0.8; or combined opacity of several overlays <= 0.8. Otherwise the touch is dropped and logged [S1].
- Android 12+ blocks touches from non-trusted overlays of another UID by default. For SAW, only layers with opacity >= 0.8 are blocked [S3].
- Logcat text for a blocked touch: "Untrusted touch due to occlusion by PACKAGE_NAME" [S6, search excerpt].
Impact on app:
- Use TYPE_APPLICATION_OVERLAY with FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE and one full-screen black window. Keep alpha <= 0.8 so taps reach the app below. The exact 0.8 boundary is UNVERIFIED on device (S1 says "below or equal", S3 says ">= 0.8 blocked").
- Max darkening with touch-through is 80% black from one overlay. Going darker means giving up touch-through. This is a product decision.
- Permission flow: check Settings.canDrawOverlays; if false, open ACTION_MANAGE_OVERLAY_PERMISSION. It lands on the top-level list, so show in-app steps that name the app. A direct deep link to the app's toggle is not supported per S5.

2. ANDROID 14, 15, 16, 17 CHANGES
Facts:
- Android 14 (API 34): each foreground service must declare a foregroundServiceType. specialUse is new in 14 [S7].
- specialUse: "Covers any valid foreground service use cases that aren't covered by the other foreground service types." Needs the FOREGROUND_SERVICE_SPECIAL_USE permission (normal, granted by default, not revocable) and <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="..."/> inside <service>. Values are reviewed in Play Console at submission [S8][S7].
- No type declared: startForeground() throws MissingForegroundServiceTypeException [S7].
- Apps targeting 31+ cannot start an FGS from the background except for exemptions. Exemptions include a user action on a UI element related to the app (bubble, notification, widget, activity). Quick Settings tiles are not named [S9].
- Android 15 (API 35): an app holding SYSTEM_ALERT_WINDOW can start an FGS from the background only if it already has a visible TYPE_APPLICATION_OVERLAY window. Otherwise ForegroundServiceStartNotAllowedException [S10].
- Android 15 BOOT_COMPLETED receivers may not start dataSync, camera, mediaPlayback, phoneCall, mediaProjection or microphone FGS. specialUse is not in that list [S10].
- POST_NOTIFICATIONS: runtime permission from Android 13 (API 33). Not needed to start an FGS, but a notification must be shown when an FGS starts [S11]. If denied, FGS notices are hidden from the drawer but still shown in Task Manager [S12].
- Android 16 (API 36): the all-apps and targeting-36 lists show no overlay, FGS-type, sideloading or notification-permission change. Only health-permission, JobScheduler and similar changes were found [S13][S14].
- Android 17 (API 37): stable, released 16 June 2026 [S15][S16]. Its behavior-change list has no overlay, FGS-type, sideloading or notification change. Relevant items: background audio needs a while-in-use FGS for targetSdk 37 [S16]; BAL hardening, prefer MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE [S16].
- Sideloading: Google developer verification. Enforcement starts 30 Sept 2026 in Brazil, Indonesia, Singapore and Thailand, and is planned globally for 2027. Unverified developers can still sideload via an advanced flow [S17].
- Google Play: from 31 Aug 2026 new apps and updates must target API 36+ [S18].
Impact on app:
- Declare android:foregroundServiceType="specialUse", the FOREGROUND_SERVICE_SPECIAL_USE permission, and the PROPERTY with a plain-language reason (for example: "shows a dimming layer over other apps at the user's request"). Expect Play review of that text.
- Start the overlay service only from a visible activity (the single button). This avoids the Android 15 SAW rule.
- Request POST_NOTIFICATIONS in context before starting the FGS. Without it the notification is hidden from the drawer.
- Target API 36+ for Play. compileSdk 37 is fine.
- APK distributed outside Play: verification applies in the four countries from 30 Sept 2026. Elsewhere it is not yet enforced; the global rollout is planned for 2027 [S17].

3. QUICK SETTINGS TILE (TileService)
Facts:
- onClick() runs on tap when the tile is ACTIVE or INACTIVE. The app may then "launch a dialog or activity, trigger background work, or change the state of your tile" [S19].
- showDialog() collapses the panel. startActivityAndCollapse(PendingIntent) starts an activity. The PendingIntent needs FLAG_ACTIVITY_NEW_TASK from API 28 [S19][S20].
- unlockAndRun(Runnable) prompts unlock first [S20]. requestAddTileService() exists from Android 13 [S19].
- Neither the TileService reference nor the tile guide says whether a tile tap permits a background FGS start. The exemption list does not name tiles [S9]. UNVERIFIED.
Impact on app:
- Safer pattern: tile onClick -> startActivityAndCollapse to a visible transparent activity -> that activity starts the overlay FGS. Test on device; UNVERIFIED that this works on all target devices.
- Ship the in-app button first. Make the tile an optional extra.
- Use a toggle tile (TOGGLEABLE_TILE meta-data) for on/off [S19].

4. SYSTEM BRIGHTNESS
Facts:
- Settings.System.SCREEN_BRIGHTNESS: "between 1 (minimum) and 255 (maximum)". The page says to use WindowManager.LayoutParams#screenBrightness instead [S21].
- Writing system brightness needs WRITE_SETTINGS in the manifest AND a user grant through ACTION_MANAGE_WRITE_SETTINGS, which opens a permission screen. canWrite() is API 23 [S21].
- WindowManager.LayoutParams.screenBrightness: no permission. Less than 0 = user's preferred brightness; 0 to 1 = "from dark to full bright" [S1].
- BRIGHTNESS_OVERRIDE_OFF (0.0): backlight set to "the lowest value" while the window is in front [S1].
- No documented value goes below the hardware minimum via either API. Whether 0.0 equals the hardware minimum on a device is UNVERIFIED.
- Extra dim: "dim your screen beyond your phone's minimum brightness". Pixel 9 and earlier: Settings > Accessibility > Extra dim. Pixel 10+: inside the brightness slider. The page gives no Android version [S22]. Android 12 origin is SECONDARY [S23][S24].
Impact on app:
- Do not use WRITE_SETTINGS. It needs a user grant and cannot go below minimum.
- The overlay is the only documented way to go darker than the minimum.
- Extra dim is a per-device user setting. Apps cannot control it. Plain-language guidance can mention it on Pixel only.

5. "REDUCE BRIGHT COLORS" / EXTRA DIM SETTINGS KEYS
Facts:
- Settings.Secure: "Normal applications cannot modify the secure settings database, either directly or by calling the 'put' methods" [S25].
- WRITE_SECURE_SETTINGS: "Allows an application to read or write the secure system settings. Not for use by third-party applications." [S2].
- The public Settings.Secure page (fetched text) lists no reduce_bright_colors or Extra dim constant.
- The official AOSP Settings XML (2020, commit 879babf246) uses the preference keys "rbc_intensity" and "rbc_persist". It does NOT contain reduce_bright_colors_activated or reduce_bright_colors_level [S26].
- AOSP-derived mirror code (search excerpt, not upstream-verified) shows hidden @hide constants REDUCE_BRIGHT_COLORS_ACTIVATED ("reduce_bright_colors_activated") and REDUCE_BRIGHT_COLORS_LEVEL (range 0-100) [S27]. UNVERIFIED.
- Introduction: Android 12, per SECONDARY reporting (2021: "Reduce bright colors" in Developer Preview, later "Extra dim") [S23][S24]. No official Android 11 mention found.
Impact on app:
- A normal app cannot set these. Do not design around key names. Use the overlay instead.

6. ACCESSIBILITY SERVICE OVERLAY (TYPE_ACCESSIBILITY_OVERLAY)
Facts:
- TYPE_ACCESSIBILITY_OVERLAY: added API 22, value 2032. For windows overlaid only by a connected AccessibilityService [S1].
- Accessibility windows are "trusted" for FLAG_NOT_TOUCHABLE: the 0.8 opacity limit is not applied to them [S1].
- Service setup: BIND_ACCESSIBILITY_SERVICE permission, intent filter android.accessibilityservice.AccessibilityService, and an XML config [S28].
- Official policy: "Only build an accessibility service if you are creating a general-purpose assistive tool" [S28]. "Your app must use platform-level accessibility services only for the purpose of helping users with disabilities interact with your app" [S29].
- Enable flow: the user switches the service on in settings (codelab excerpt; official wording not fetched) [S30].
- Android 13 restricted settings: official Android 13 behavior pages do not mention it [S31][S12]. SECONDARY reports say apps installed outside an app store are blocked from accessibility, notification-listener and (per Android 15 reports) draw-over-apps until the user taps "Allow restricted settings" in App info > menu. The option appears only after a blocked attempt [S32][S33][S34][S35]. UNVERIFIED on official docs.
Impact on app:
- Do not use an accessibility overlay. The use case conflicts with the assistive-tool rule [S28][S29]. Play accessibility policy is UNVERIFIED (not fetched). Sideloaded installs are blocked until the user changes restricted settings.
- For a sideloaded TYPE_APPLICATION_OVERLAY app, the "display over other apps" toggle may also be restricted (SECONDARY, UNVERIFIED). Route the user to the app's App info page, where the option is shown; menu names vary by OEM.

7. CURRENT STABLE VERSIONS (checked 2026-11-23)
- compileSdk: API 37 (Android 17). Stable since 16 June 2026 [S15][S16]. Cached platforms page (S42) is stale and has no API 37 entry.
- Android Gradle Plugin: 9.5.0 (October 2026). Supports up to API 37; minimum Gradle 9.6.0 [S36]. AGP 9.6 notes list alpha builds only, so stable status is UNVERIFIED [S37]. A "9.5.1" heading appears on S36 with no date; UNVERIFIED.
- Gradle: 9.9.2, released 19 Nov 2026, the latest supported release [S38].
- Kotlin: 2.4.21, released 8 Oct 2026 [S39]. Page footer date is inconsistent; verify on GitHub.
- androidx.core: 1.19.1 (23 Sept 2026) stable; previous 1.19.0 (3 June 2026) [S40].
- androidx.activity: 1.13.0 (11 March 2026) is the latest stable. 1.14.0-alpha03 (23 Sept 2026) is an alpha [S41].
- Google Play target: API 36+ required for new apps and updates from 31 Aug 2026 [S18].

UNVERIFIED LIST
- Exact 0.8 touch-through boundary on device.
- Whether a Quick Settings tile tap permits background FGS start.
- Whether window brightness 0.0 equals the hardware minimum on a given phone.
- Reduce bright colors key names in upstream frameworks.
- Official Android 13/15 text on restricted settings, and whether it covers draw-over-apps.
- AGP 9.6.0 and 9.5.1 stable status.
- Blocked-touch Logcat text (only from a search excerpt).

## Açık kaynak rakip uygulamalar
# Survey: open-source Android screen dimmers (below-minimum brightness)

Scope: read source for 8 public repos (shallow clones at `/tmp/claude-0/-home-user/28c5e01d-6ed1-5e5c-a541-fd0d40b2de76/scratchpad/src/`). Line numbers refer to the HEAD commits listed. Dates are relative to 2026-11-23. Device behavior was not tested in this run.

## Key findings

- **Overlay-only apps (work over other apps, no ADB/root):** Red Moon, code-crusher/dimmer, MidnightBeam (overlay mode), Circadian Display (overlay mode), OpenLumen (overlay driver).
- **Below-minimum without ADB:** Red Moon's lower-brightness option writes `SCREEN_BRIGHTNESS = 0` after a WRITE_SETTINGS grant. Its effect on a real device is UNVERIFIED.
- **Android 12+ overlay cap:** the system limits obscuring opacity for tap-through overlays. Code uses a default of 0.8. Circadian caps at 0.76 on Android 12+, and OpenLumen says "capped at ~80%". An overlay that keeps taps working on Android 12+ tops out near 80% black.
- **Settings.Secure "Extra Dim" / Reduce Bright Colors** goes below the panel minimum on Android 12+, but needs WRITE_SECURE_SETTINGS. That is granted only via adb, Shizuku, or root, which conflicts with the no-technical-setup requirement.
- **gloomy** needs no permissions but only dims inside its own window, so it does not cover Instagram or games.
- **Permission deep links exist:** `ACTION_MANAGE_OVERLAY_PERMISSION` (Red Moon `Permission.kt:80-81`, code-crusher `PermissionChecker.java:28`) and `ACTION_MANAGE_WRITE_SETTINGS` (Red Moon `Permission.kt:107-108`).

## Per-project facts

**1. Red Moon** (GitHub `raatmarien/red-moon` redirects to `https://github.com/LibreShift/red-moon`, HEAD d4d1c44)
- License: GPL-3.0-or-later (`LICENSE.md`, README). Language: Kotlin.
- minSdk 14, targetSdk 32, compileSdk 32 (`app/build.gradle:31-36`).
- Last commit 2025-12-08. README: "no longer maintained, but Pull Requests are still accepted."
- Permissions (manifest): SYSTEM_ALERT_WINDOW, WRITE_SETTINGS, ACCESS_FINE/COARSE_LOCATION, SCHEDULE_EXACT_ALARM, GET_TASKS, PACKAGE_USAGE_STATS, FOREGROUND_SERVICE, RECEIVE_BOOT_COMPLETED.
- Dimming:
  - Overlay type is `TYPE_ACCESSIBILITY_OVERLAY` if the accessibility service is on, else `TYPE_APPLICATION_OVERLAY` (API 26+), else `TYPE_SYSTEM_OVERLAY` (`app/src/main/java/com/jmstudios/redmoon/Overlay.kt:117-124`). Flags include `FLAG_NOT_TOUCHABLE` (line 124). Draws `canvas.drawColor(profile.filterColor)` (line 135).
  - Darkness: `DIM_MAX_ALPHA = 0.9f` (`Profile.kt:102`). From the blend formula at line 89, dim 100 with intensity 0 gives alpha 0.9. This is my arithmetic, not device-tested.
  - Brightness: `BrightnessManager.kt:42-45` writes `Settings.System.SCREEN_BRIGHTNESS`. It sets level 0 when `lowerBrightness` is on (line 72). Default is off (`Config.kt:83`). Requires WRITE_SETTINGS.
  - Root mode: SurfaceFlinger color matrix via libsu `Shell.su` (`surfaceflinger/SurfaceFlinger.kt:24-45`). Off by default (`Config.kt:205`).
- Features: sunset-to-sunrise schedule (`service/LocationUpdateService.kt`, `receiver/ScheduleReceiver.kt`); profiles (`Profile.kt`, default intensity 30 and dim 40, `Config.kt:75-80`); quick-settings tile (`widget/TileReceiver.kt`); home widget (`widget/SwitchAppWidgetProvider.kt`); notification. Per-app pause: whitelisted app sends PAUSE (`manager/CurrentAppMonitor.kt:49`), using UsageStats or ActivityManager (`CurrentAppChecker.kt:36-73`). The accessibility service sets `canRetrieveWindowContent="false"` (`res/xml/accessibility_service_config.xml`).
- Touch on Android 12+ at alpha up to 0.9: UNVERIFIED. `Overlay.kt` has no opacity-ceiling logic.

**2. code-crusher/dimmer** (`https://github.com/code-crusher/dimmer`, HEAD c81ee7b)
- License: no LICENSE file in the repo, and no license text in the README. Treat as all rights reserved.
- Language: Java. minSdk 14, targetSdk 25, compileSdk 25 (`app/build.gradle:14-15, 9`).
- Last commit 2018-11-13. 36 commits. Not maintained.
- Permissions: WRITE_SETTINGS (declared but no Settings.System calls found), SYSTEM_ALERT_WINDOW, INTERNET, ACCESS_NETWORK_STATE, PERSISTENT_ACTIVITY, RECEIVE_BOOT_COMPLETED (manifest lines 5-10). Includes Fabric/Crashlytics Answers analytics (`app/build.gradle:2,43-46`, `ScreenDimmer.java:27,32`).
- Dimming:
  - Two SurfaceViews added with `TYPE_SYSTEM_OVERLAY`, MATCH_PARENT (`service/ScreenDimmer.java:118-132`).
  - Code sets `FLAG_NOT_TOUCHABLE` (line 127), then overwrites `params.flags` with only `FLAG_LAYOUT_IN_SCREEN` (line 129). By the code, the overlay is therefore touchable. Device test not done.
  - Slider max is 89 (`helper/Constants.java:11`, `MainActivity.java:154`). `Helper.getColorInt` makes `"#"+%02d+"000000"` (`Helper.java:20-21`), so 89 gives alpha 0x89, about 54% black.
  - Warm tint `#5e3f4000` is always on while running (`res/values/colors.xml:20`, `ScreenDimmer.java:120,91`).
- Features: manual slider; ambient-light sensor (TYPE_LIGHT) pauses the dimmer when lux > 80 and opens `AlertDialogActivity` (`ScreenDimmer.java:138-143, 220-226`; `Constants.java:30`); start/stop alarms (`MainActivity.java:250-253`); notification pause/stop/start (`ScreenDimmer.java:160-193`); boot receiver disabled by default (manifest line 37).
- Overlay permission deep link: `helper/PermissionChecker.java:22-28`.

**3. MidnightBeam** (`https://github.com/MadeinTaly/midnightbeam`, HEAD 2553e9b)
- License: MIT (`LICENSE`). Language: Java. Build via `build.sh`, not Gradle.
- minSdk 26, targetSdk 35 (`app/AndroidManifest.xml:7`).
- Last commit 2026-11-19. 52 commits. Active.
- Permissions (manifest lines 9-31): SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE(+SPECIAL_USE), RECEIVE_BOOT_COMPLETED, INTERNET, CHANGE_WIFI_MULTICAST_STATE, WRITE_SECURE_SETTINGS, DUMP, MODIFY_AUDIO_SETTINGS, REQUEST_INSTALL_PACKAGES.
- Dimming:
  - `DimService.showOverlay` (line 1267). Uses `TYPE_ACCESSIBILITY_OVERLAY` (2032) or `TYPE_APPLICATION_OVERLAY` (2038), with NOT_FOCUSABLE|NOT_TOUCHABLE (lines 1293-1301).
  - Android 12+ clamp applies only to app overlays (`if (!acc && SDK >= 31)`, lines 1274-1285). The cap comes from `Compat.maxObscuringOpacity` (`Compat.java:48-55`, fallback 0.8). The accessibility-overlay path has maxAlpha = 1, so it is uncapped in code. Touch behavior for that path is UNVERIFIED.
  - Overlay color: `overlayColor()` makes `(100-bright)%` black (`DimService.java:1115-1126`). `bright` range is 5-100 (lines 41, 232), so 95% black is reachable in code on pre-12 or accessibility mode.
  - Accessibility service enabled via WRITE_SECURE_SETTINGS (`Compat.java:72-86`). Service sets `canRetrieveWindowContent="false"` (`app/res/xml/accessibility_service.xml`).
  - Advanced filter: SurfaceFlinger color matrix via `su` (`DimService.java:1145-1148`, `Priv.java:21,142`). UI text says "never goes below 30% brightness" (`MainActivity.java:1342`).
- Features: warm color temperature and manual sliders; presets "Evening"/"Night" (`DimService.java:216-218`); schedule (line 208); sleep timer with optional webhook (`DimService.java:838-844`, `MainActivity.java:705-724`, marked Experimental); audio equalizer (`AudioFx.java`); HTTP-style remote server (`RemoteServer.java`); BootReceiver (manifest:103).

**4. Circadian Display** (`https://github.com/tomar-mohit/circadian-display`, HEAD 16fb1d4)
- License: Apache-2.0 (`LICENSE`). Language: Kotlin.
- minSdk 26, targetSdk 35, compileSdk 35 (`app/build.gradle.kts:13,22-23`). versionName in repo is 2.0.1 (line 25). The F-Droid page shows 2.0.2.
- Last commit 2026-11-15. 33 commits. Active.
- Permissions: SYSTEM_ALERT_WINDOW, WRITE_SECURE_SETTINGS, PACKAGE_USAGE_STATS, SCHEDULE_EXACT_ALARM, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, RECEIVE_BOOT_COMPLETED, ACCESS_NETWORK_STATE.
- Dimming:
  - Overlay: `TYPE_APPLICATION_OVERLAY` (or `TYPE_PHONE` pre-O), FLAG_NOT_TOUCHABLE, FLAG_NOT_FOCUSABLE (`system/overlay/.../OverlayDisplayController.kt:155-170`, addView at 187). One view draws black dimming and amber warmth (warmth max alpha 0.5, line 208).
  - Android 12+ cap: `windowAlpha = system max × 0.95` (lines 88-95, 224), default 0.8 (line 216), so 0.76. Presented dimming is clamped to windowAlpha (`OverlayOpacityPolicy.kt:58-59`).
  - Native mode: writes `Settings.Secure` night_display keys (`system/native/.../NativeDisplayController.kt:237, 259, 281`). Needs WRITE_SECURE_SETTINGS via ADB, with the command shown in-app (`NativeDisplayController.kt:31-32`; `SettingsScreen.kt:182-185`).
- Features: curve-based warmth and dimming over the evening; background scheduler; two modes (overlay and native).

**5. gloomy** (`https://github.com/mority/gloomy`, HEAD 0dc6094)
- License: MIT (`LICENSE`, "Copyright (c) 2026 mority"). Language: Kotlin, Compose.
- minSdk 26, targetSdk 37, compileSdk 37 (`app/build.gradle.kts:16-21`).
- Last commit 2026-11-17. 27 commits. Active.
- Permissions: none. The manifest has no `uses-permission` (`app/src/main/AndroidManifest.xml`).
- Dimming: not an overlay. A full-screen Compose activity draws a color (`MainActivity.kt:139-154`). Window brightness override is set via `window.attributes.screenBrightness`, floored at 0.01 (lines 46-47, 170-174). Color scaled to 5% at level 0 (lines 49-50, 252-255).
- Scope: "This overrides the system brightness only while this window is in the foreground" (`MainActivity.kt:124-125`). It does not stay on over other apps.
- Features: vertical swipe or volume keys for brightness (lines 194-202, 219-241); horizontal swipe for color red to white; quick-settings tile (`GloomyTileService.kt`); shows over lock screen (manifest line 14); remembers last settings (lines 104-106, 208-211).

**6. OpenLumen** (`https://github.com/SysAdminDoc/OpenLumen`, HEAD 32c86a6, v0.8.0)
- License: GPL-3.0-or-later (`LICENSE`, README badge). Language: Kotlin.
- minSdk 26, targetSdk 35, compileSdk 37 (`app/build.gradle.kts:88, 92-93`).
- Last commit 2026-11-15. 383 commits. Active.
- Permissions (declared): SYSTEM_ALERT_WINDOW, WRITE_SECURE_SETTINGS, FOREGROUND_SERVICE(+SPECIAL_USE), POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, SCHEDULE_EXACT_ALARM, ACCESS_COARSE/FINE_LOCATION, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, QUERY_ADVANCED_PROTECTION_MODE. INTERNET and ACCESS_BACKGROUND_LOCATION are declared then removed with `tools:node="remove"` (manifest lines 27-31), so the merged manifest should not request them. Not built to confirm.
- Dimming, four drivers (README table, line 50-55):
  - `Overlay` (no root): `TYPE_APPLICATION_OVERLAY` with NOT_TOUCHABLE (`core-engine/.../engines/OverlayEngine.kt:164-172`). Comment says "capped at ~0.8 when FLAG_NOT_TOUCHABLE is set" (lines 23-29).
  - `SecureSettings` (no root but needs WRITE_SECURE_SETTINGS): writes `reduce_bright_colors_level` and `_activated` (`SecureSettingsEngine.kt:438-447`) and Night Light keys. Dims below the panel minimum on Android 12+ devices with Extra Dim (README line 61).
  - `SurfaceFlinger` and `KCAL`: root.
  - Grant for SecureSettings: `adb shell pm grant com.openlumen android.permission.WRITE_SECURE_SETTINGS`, or Shizuku (README line 59).
- Features: sunset/sunrise schedule by location (README); four preset quick-settings tiles and grayscale tile (manifest lines 116-197, `service/LumenTileService.kt`); home widget (`WidgetBridge.kt`); per-channel RGB and gamma (root drivers only); auto driver selection (README line 65); Tasker/ADB intents, off by default (README).

**7. Tideo Auto Brightness** (`https://github.com/faded-penguin021/Tideo-Auto-Brightness`, HEAD 0244923)
- License: MIT (`LICENSE`). Language: Kotlin.
- minSdk 31, targetSdk 36 (`app/build.gradle.kts:14-15`).
- Last commit 2026-11-19. 281 commits. Active.
- Type: auto-brightness app, not a blue-light filter.
- Below-minimum: `SuperDimmingCoordinator.kt` applies `reduce_bright_colors` below a threshold (lines 10, 47, 86-134). Gated on WRITE_SECURE_SETTINGS (line 78, "off: needs WRITE_SECURE_SETTINGS"). F-Droid says the user grants it via adb, Shizuku, or root.
- Overlay: none for dimming. Only `AabToastAccessibilityService.kt:19,55` draws a presentation overlay. No SYSTEM_ALERT_WINDOW in manifest.
- Permissions (manifest): WRITE_SECURE_SETTINGS, WRITE_SETTINGS, PACKAGE_USAGE_STATS, ACCESS_FINE/COARSE/BACKGROUND_LOCATION, INTERNET, DUMP, VIBRATE, RECEIVE_BOOT_COMPLETED, FOREGROUND_SERVICE.

**8. pariseed/lux** (`https://github.com/pariseed/lux`, HEAD a406bae)
- Excluded. Last commit 2023-12-28, 4 commits. Python/Buildozer. `main.py:8` only fires `com.android.intent.action.SHOW_BRIGHTNESS_DIALOG`. Not a dimmer.

## Not verified (UNVERIFIED)

- **marshmeadow/dimtv:** GitHub repo unreachable. WebFetch 404, git clone auth prompt, `add_repo` returned "not found or no access". Only search-snippet claims remain: overlay below floor, Apache-2.0, F-Droid 3.3.1 (37) dated 2015-12-03, Android 4.0+. Source not read.
- **D4rK7355608/com.d4rk.lowbrightness:** same access failure. Third-party only: chrome-stats says SYSTEM_ALERT_WINDOW and WRITE_SETTINGS are high-risk permissions; version 5.1.0 updated 2026-01-26; Android 10+. License and code UNVERIFIED.
- **Lux (com.dimmerpro.app):** Play Store only. No source link found. Not counted as open source.
- **crDroid brightness tweaks:** Magisk module, root. Not read.
- **Official Android values:** `developer.android.com` InputManager reference returned 404. Medium "Untrusted Touch Events" returned 403. The API-diff page confirms `getMaximumObscuringOpacityForTouch` was added in API 31 (`developer.android.com/sdk/api_diff/31/changes/android.hardware.input.InputManager`). The 0.8 default is from code only, and the runtime value is device-reported.
- **WRITE_SECURE_SETTINGS protection level and grant rules:** the reference page section was not reached. The adb/Shizuku/root route rests on OpenLumen's README and Tideo's F-Droid text.
- **Red Moon's real-device darkness below the minimum and its touch behavior at alpha 0.9:** not tested.
- **code-crusher touch interception:** inferred from the flag overwrite, not tested.

## Implications for the requirements

- A rootless, ADB-free overlay that stays tap-through on Android 12+ maxes out near 80% black. Lower than that needs one of the paths below.
- The clean non-overlay option is Settings.Secure Extra Dim, which needs WRITE_SECURE_SETTINGS. That step has no in-app grant screen, so it conflicts with the no-technical-setup rule.
- Red Moon's SCREEN_BRIGHTNESS=0 path is the only verified non-ADB system-brightness route here. It needs WRITE_SETTINGS and is deep-linkable. It is system-wide, so the user's brightness is changed.
- Reuse licensing: GPL-3.0-or-later (Red Moon, OpenLumen) is copyleft. MIT (MidnightBeam, gloomy, Tideo) and Apache-2.0 (Circadian) are permissive. code-crusher has no license, so do not reuse its code.

## Kullanıcı şikayetleri ve beğeniler
RESEARCH REPORT: Android screen-dimmer user feedback (as of 2026-11-23)

SCOPE AND LIMITS
- Sample is small and skewed toward open-source apps with public issue trackers. Read directly: Red Moon GitHub tracker (63 open issues; 50 titles on pages 1-2, plus 6 issue pages), Screen & Notifications Dimmer tracker (7 open), Tideo tracker (3 open), MidnightBeam tracker (1 open), Circadian Display tracker (0 open), gloomy tracker (4 open), F-Droid forum thread, Mozilla Bugzilla 1189478, F-Droid listings (Tideo, Circadian Display, MidnightBeam), Lux privacy policy.
- Frequency below = number of sources in my sample, not share of users.
- Google Play reviews: NOT readable for any target. Lux and Low Brightness Play pages truncated before reviews (https://play.google.com/store/apps/details?id=com.dimmerpro.app&showAllReviews=true , https://play.google.com/store/apps/details?id=com.d4rk.lowbrightness&showAllReviews=true). AppBrain, Chrome-Stats reviews and AppFollow returned 403 or timed out.
- Reddit: no evidence. WebSearch refused the reddit.com domain filter, and unfiltered searches returned no Reddit threads. UNVERIFIED.
- XDA: no matching thread found.
- Unverified bodies: Samsung S21 thread (title confirmed at https://community.samsung.com/uk/conversations/other-galaxy-s-series/overlay-apps-not-working-on-s21-ultra-running-oneui-4-0-and/eu_4411200 , body truncated; touch quote is from search snippet only). Unity filter-touch bug page returned 404 (UNVERIFIED). Phonearena Android 8 article and ASUS threads returned 403 (snippet only). DimTV README returned 404 (UNVERIFIED).

(a) TOP 10 COMPLAINT THEMES (ranked by sources in sample; approximate)

1. Overlay blocks taps or leaves parts of the screen dead (home screen, games, login prompts, other apps) [~6 sources]
- "I cannot touch anywhere on home screen to pull up drawer, click on apps to open or swipe to other screens." Red Moon #309, Android 12 (opened Oct 2021, closed Dec 11 2024 via PR #329): https://github.com/raatmarien/red-moon/issues/309
- Red Moon #293, overlay blocks clicks on Google 2FA confirmation (open, title only): https://github.com/raatmarien/red-moon/issues?page=2
- Red Moon #333, KeePassXD unusable with filter on (open, title only): https://github.com/raatmarien/red-moon/issues
- "the touch screen is unresponsive except for the top where the icons are still purely white." F-Droid forum, Jan 9 2022: https://forum.f-droid.org/t/night-light-apps-make-my-touch-screen-unrespinsive/16507
- Firefox Nightly ignored all input while a filter (Twilight, Bluelight Filter named) was on; pausing the filter fixed it. Closed as duplicate of fixed bug 1187547 (2015): https://bugzilla.mozilla.org/show_bug.cgi?id=1189478
- Circadian Display 2.0.1 changelog: "Clamp combined overlay opacity below touch pass-through ceiling (Fixes #8)": https://f-droid.org/packages/com.circadiandisplay.app/
- Samsung S21 Ultra, One UI 4 / Android 12: overlay apps stopped passing touch through (UNVERIFIED body, search snippet): https://community.samsung.com/uk/conversations/other-galaxy-s-series/overlay-apps-not-working-on-s21-ultra-running-oneui-4-0-and/eu_4411200

2. Filter turns itself back on; schedule overrides manual off; toggles unreliable [~5]
- "It often immediately turns back on." Red Moon #319 (Jul 2022, Moto G8 Power, Android 11; still open; a 2026 comment reports the same on Android 12 TV): https://github.com/raatmarien/red-moon/issues/319
- Manual changes revert at sunrise/sunset (#320), paused filter resumes on crossing a boundary (#331), root-mode filter stays off until a value is edited (#346): https://github.com/raatmarien/red-moon/issues
- Changing profile while paused does not refresh the notification (#210, 2017, open): https://github.com/raatmarien/red-moon/issues?page=2
- Screen Dimmer FAQ: accessibility service switches itself off through battery optimization; "Screen Dimmer has no control over this": https://ericytsang.github.io/app.android.screenfilter/faq.html

3. Crashes on reboot or after OS/device changes [~4]
- "Not just one time crash, it crashes on every device restart." Red Moon #306 (Sep 2021, Android 8.1). Maintainer said fixed in code; still listed open: https://github.com/raatmarien/red-moon/issues/306
- Crash on settings change (#332, Nov 2022) and "Find a crash!" (#288, 2021), both open, titles only: https://github.com/raatmarien/red-moon/issues and https://github.com/raatmarien/red-moon/issues?page=2
- Android 12 breakage: see #309 above. ASUS forum (snippet, UNVERIFIED): a dimming app stopped working after Android 12, while another user said a screen filter still works: https://zentalk.asus.com/en/discussion/comment/223894
- Android 8.0 overlay change: Twilight lost filtering of status bar, nav bar, lock screen and notification shade (news article, UNVERIFIED body): https://www.phonearena.com/news/Android-O-breaks-apps-like-Twilight-and-Status-that-draw-over-the-system-UI_id92983

4. Accessibility and overlay permission confusion; repeated prompts [~3]
- Screen & Notifications Dimmer issue "Dimmer needs accessibility access" (Nov 2020; user objects; title only): https://github.com/ericytsang/app.android.screenfilter/issues
- FAQ: "Screen Dimmer keeps asking to turn on the accessibility service, but it's already on!" Fix offered: restart the device: https://ericytsang.github.io/app.android.screenfilter/faq.html
- Mobizen support: the overlay popup is tied to blue-light filter apps; advice is to force-stop or uninstall the filter (search snippet, UNVERIFIED body): https://helpdesk.mobizen.com/hc/en-us/articles/15317444143379

5. Needs ADB, root, Shizuku or a secure-settings grant (technical setup) [~4]
- The Red Moon Android 12 fix is a computer command: "adb shell settings put global block_untrusted_touches 0": https://github.com/raatmarien/red-moon/issues/309
- #343 asks whether root mode can be replaced by granting WRITE_SECURE_SETTINGS over ADB (open): https://github.com/raatmarien/red-moon/issues
- Tideo: WRITE_SECURE_SETTINGS "is never granted automatically. You grant it yourself via adb, Shizuku, or root": https://f-droid.org/packages/com.tideo.autobrightness/
- Circadian Display and MidnightBeam also request WRITE_SECURE_SETTINGS: https://f-droid.org/packages/com.circadiandisplay.app/ ; https://f-droid.org/packages/dev.midnightbeam/
- Direct conflict with the "no technical settings" goal.

6. Intrusive ads in dimmer apps [4, all aggregator or review snippets]
- Night Screen: Ultra Dim Mode: one July 2026 review calls its ads "predatory" and says the developer defends premium pricing. Single review; UNVERIFIED beyond snippet: https://www.appbrain.com/app/night-screen-ultra-dim-mode/com.androxus.screendimmer
- Brightness Dimmer Pro: newer full-screen ads appear when touching controls (snippet): https://chrome-stats.com/d/com.util.brightnesscontroller
- Screen Light Dimmer & Filter: "Frequent and intrusive ads, especially during navigation and setting changes" (snippet): https://chrome-stats.com/d/com.brigdimmer.scrlghfilter
- Lower Brightness Screen Filter listed as containing ads (snippet): https://www.appbrain.com/app/lower-brightness-screen-filter/nu.lower.brightness

7. Paywalled dimming levels and premium pricing [3]
- Super Dim: "several dimming options require payment, which some users view as overpriced" (aggregator summary): https://chrome-stats.com/d/com.tasca.superdim
- Night Screen developer defends premium pricing (review snippet, see theme 6).
- Lux uses paywalls via Adapty (privacy policy, fetched): https://hardikjain826.github.io/app-policies/dimmer-pro/privacy-policy
- Users cannot tell whether an in-app purchase removes ads, supports the developer, or unlocks basics (Android Authority, snippet): https://androidauthority.com/free-apps-getting-worse-3612373

8. Background activity, auto-start and battery [2, one detailed]
- Red Moon #316 (May 2022, MIUI 11, Android 7.1.2, open): "I can see that the app auto start randomly in background , why??" Device flagged heavy background use. No battery figures given: https://github.com/raatmarien/red-moon/issues/316
- Screen Light Dimmer & Filter: "App auto-starts and dims screen after device restart without user control" (snippet): https://chrome-stats.com/d/com.brigdimmer.scrlghfilter
- Battery is a thin theme in this sample.

9. Quick Settings tile and widget do not work [~4]
- "Quick setting not working" (Red Moon #292, Apr 2021, open): https://github.com/raatmarien/red-moon/issues?page=2
- "Cannot add the widget" (Red Moon #358, Sep 29 2026, open): https://github.com/raatmarien/red-moon/issues
- Notification toggle "effectively unusable" because it reverts too fast (#319, above)
- gloomy: "Quick settings tile issues" (open): https://github.com/mority/gloomy/issues

10. Stuck, bricked or unusable after uninstall or reboot [rare, severe; 1 detailed report]
- Red Moon #335 (Dec 2022, OnePlus 8T, Android 12.1, root granted): after uninstalling, the phone was "bricked"; "I had to wipe all my data and reinstall a clean version of Android." Single report, open: https://github.com/raatmarien/red-moon/issues/335
- Black screen after reboot with a stuck dim overlay: NOT FOUND. UNVERIFIED as a dimmer-specific theme.

(b) TOP 10 PRAISED FEATURES
Note: only two real user praise statements were found. The rest are listing claims or aggregator summaries, labeled as such.
1. Dims below the system minimum, usable in very low light. User: "I installed Dimmer from F-Droid and use it in very low light conditions" (forum snippet, UNVERIFIED body): https://forum.fairphone.com/t/screen-brightness/73185
2. Works on Android 12 for some users. User: "I use darker (screen filter) that works on android 12" (snippet, UNVERIFIED body): https://zentalk.asus.com/en/discussion/comment/223894
3. Highest-rated in sample: Screen & Notifications Dimmer, about 4.61 from about 17,500 ratings (aggregator snippet): https://chrome-stats.com/d/com.github.ericytsang.screenfilter.app.android
4. Auto scheduling, per-app dimming, hex color filter, "generally low ads" (aggregator summary, not verified): https://chrome-stats.com/d/com.github.ericytsang.screenfilter.app.android
5. Free and ad-free. Developer claim: "No ads today, no ads ever" (Play snippet). Privacy policy describes no advertising (fetched): https://play.google.com/store/apps/details?id=com.dimmerpro.app ; https://hardikjain826.github.io/app-policies/dimmer-pro/privacy-policy
6. Open source with no F-Droid anti-feature tags (ads, tracking) on Tideo, Circadian Display and MidnightBeam. Listing fact, not user praise: https://f-droid.org/packages/com.tideo.autobrightness/ ; https://f-droid.org/packages/com.circadiandisplay.app/ ; https://f-droid.org/packages/dev.midnightbeam/
7. Live adjustment with no flash to full brightness. Developer claim: "change live, with no flash to full brightness": https://f-droid.org/packages/dev.midnightbeam/
8. Adjustable color temperature, intensity and dim levels, free on F-Droid (editorial, not user review): https://www.phonearena.com/news/Spotlight-Red-Moon-for-Android-is-a-great-blue-light-filtering-app-alternative-to-f.lux-and-Twilight_id86294
9. Brightness and warmth on user-defined curves, plus scheduled actions (developer description): https://f-droid.org/packages/com.circadiandisplay.app/
10. Many night modes in one app (AppBrain summary). An AI-generated review summary says it "dims the screen effectively"; reliability UNVERIFIED: https://www.appbrain.com/app/night-screen-ultra-dim-mode/com.androxus.screendimmer

(c) PRIVACY AND PERMISSION COMPLAINTS
- Objection to accessibility requirement: "Dimmer needs accessibility access" (Nov 2020, title only): https://github.com/ericytsang/app.android.screenfilter/issues
- Aggregator summary says Screen Dimmer reviewers worry about data access and broad permissions (snippet): https://chrome-stats.com/d/com.github.ericytsang.screenfilter.app.android
- Red Moon #248 "Add a privacy policy" (Apr 2019, still open): https://github.com/raatmarien/red-moon/issues?page=2
- Red Moon #214 (Jan 2018): a copied codebase "Trying to Sell with Different Name on Google Play." Trust complaint: https://github.com/raatmarien/red-moon/issues?page=2
- Permissions in F-Droid listings (fetched; listing facts, not complaints found): Tideo: background location, full network, DUMP, WRITE_SECURE_SETTINGS, PACKAGE_USAGE_STATS. MidnightBeam: DUMP, WRITE_SECURE_SETTINGS, full network, audio settings, Wi-Fi multicast. Circadian Display: WRITE_SECURE_SETTINGS, PACKAGE_USAGE_STATS, ignore battery optimizations, appear on top.
- Lux privacy policy (fetched): "We do not use the Accessibility Service to collect, store, or share any personal or sensitive user data." "This service is only activated with your explicit permission." Also PostHog anonymous analytics and device metadata: https://hardikjain826.github.io/app-policies/dimmer-pro/privacy-policy
- DimTV README warns that running-task access could reveal which apps the user uses (UNVERIFIED, fetch 404; search snippet only): https://github.com/marshmeadow/dimtv
- Generic accessibility risk (not app complaints): accessibility grants can expose on-screen text (search snippet, UNVERIFIED): https://www.bitdefender.com/en-au/blog/hotforsecurity/accessibility-service-an-android-blessing-and-a-security-challenge-in-the-same-package

OTHER NOTES AND CONFLICTS
- Red Moon "Maintenance Status" (#281, Dec 2020, open) suggests users doubt the project is maintained: https://github.com/raatmarien/red-moon/issues?page=2
- Make Tech Easier says Lux, Screen Dimmer, Screen Filter and Night Mode were removed from Google Play. This conflicts with a live Lux listing; UNVERIFIED: https://maketecheasier.com/screen-dimming-android-apps
- Low Brightness (com.d4rk.lowbrightness): no complaints readable. Ratings conflict across snapshots (4.25 from 66 ratings vs 4.7 from 1,000+ installs): https://chrome-stats.com/d/com.d4rk.lowbrightness
- gloomy: 4 open issues (quick settings tile, screen rotation, capitalization, color range). None about touch or ads: https://github.com/mority/gloomy/issues

IMPLICATIONS (my inference, from the sources above)
- Test touch pass-through on Android 12+ and in games before release. Red Moon's Android 12 touch bug took about 3 years to close.
- Avoid accessibility and WRITE_SECURE_SETTINGS if possible. They are the main setup barriers in this sample.
- Make the on/off state survive reboot and any schedule; users complain most about a filter that turns itself back on.
- Keep ads and paywalls off the core button. The sample shows the most anger at both.
- Give a plain-language permission explanation (Lux's disclosure is the only example of this found).

UNVERIFIED LIST
- Play Store reviews for all targets (not readable).
- Reddit and XDA (no evidence found).
- Samsung S21 touch details, Asus Android 12 quotes, Phonearena Android 8 article, Mobizen, Fairphone and Night Screen review text (search snippets only).
- Unity filter-touch bug (404).
- DimTV README (404); Red Moon usage-stats permission (search snippet only).
- Lux "no ads" and accessibility disclosure (Play page truncated; privacy policy does not mention ads).

## Oyun ve uygulama uyumluluğu
COMPATIBILITY REPORT: screen-dimming overlay (non-touchable, always-on-top, "display over other apps") vs. popular Android games and apps
Research only. No compatibility tests were run on devices.

BOTTOM LINE
- No official source found, for any of the six titles, that names screen overlays, dimmers, or "display over other apps" as a ban trigger or as an in-game warning. Full terms for Epic, Garena, Activision, HoYoverse and Meta were not read in full, so this absence is UNVERIFIED.
- "Screen overlay detected" is an OS/OEM message, not a game anti-cheat message. It appears when an overlay is active while another app's permission is being granted. Samsung says Screen Overlay must be off when granting permissions, or an error appears. https://www.samsung.com/us/support/troubleshooting/TSG10004868/ (high). Verizon describes the same conflict: https://www.verizon.com/support/knowledge-base-209672 (search snippet).
- The stronger technical constraint is Android 12+ touch blocking (see below). It affects whether the game still receives taps, not whether an account gets banned.
- The only overlay-linked ban claims found are rumours (PUBG Mobile, below).

ANDROID PLATFORM FACTS
- Android 12+ blocks touches that pass through another app's window, for all apps regardless of targetSdk. https://medium.com/androiddevelopers/untrusted-touch-events-2c0e0b9c374c (Android Developers blog, 2021-05-26). Confidence: high.
- Current docs: on Android 12+, touches from non-trusted overlays from another UID are blocked. For System Alert Window and animation layers, only touches from layers with opacity >= 0.8 are blocked. https://developer.android.com/privacy-and-security/risks/tapjacking. Confidence: high.
- Boundary conflict: the 2021 blog exempts overlays with alpha "equal to or below" 0.8, which it calls a DP3 value that "might change". The current docs block at >= 0.8. Exactly 0.8 is ambiguous, so keep alpha clearly below 0.8. Confidence: medium.
- Overlapping TYPE_APPLICATION_OVERLAY windows: their combined opacity is what counts (2021 blog). Stacking layers does not get around the limit. Confidence: medium.
- FLAG_NOT_TOUCHABLE overlays are "likely affected" unless an exemption applies (for example, sufficiently translucent). Same 2021 blog. Confidence: high.
- INFERENCE (not tested): a dimmer above about 0.8 alpha would block taps to the game beneath, and a touchable full-black layer would swallow taps. Under pass-through, the practical darkening ceiling is roughly 80% black.
- Apps targeting Android 12+ can call Window.setHideOverlayWindows(true) to keep SAW overlays off their windows (tapjacking page). Whether any of the six titles do this: UNVERIFIED.
- Permission flow: Settings.ACTION_MANAGE_OVERLAY_PERMISSION opens the "draw over other apps" control screen. The package-scoped deep link applies only before Android R, per the constant's documented input. https://developer.android.com/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION. Confidence: high. Settings.canDrawOverlays is only listed in the method table; its detail was not read (UNVERIFIED).
- UNVERIFIED: the claim that Android 8+ limits SAW windows to TYPE_APPLICATION_OVERLAY (secondary blog only). Also the logcat text "Untrusted touch due to occlusion by PACKAGE_NAME" (secondary source only).

COMPATIBILITY TABLE

| App | Behaviour with overlays | Evidence URL | Confidence |
|---|---|---|---|
| Fortnite (Android; Google Play and Epic Games Store builds) | No overlay rule or overlay-specific report found. Google Play relaunch worldwide on 2026-03-19, and Fortnite is also on the Epic Games Store for Android. Epic's FAQ answer on whether third-party software can get an account banned was not readable; the search snippet says Support will not determine or disclose this. The EULA restricts unauthorized software used for advantage (snippet only). | https://www.epicgames.com/site/news/fortnite-launches-on-google-play-worldwide-in-time-for-fortnite-showdown?lang=en ; https://www.epicgames.com/help/c-44968506/a14827285 ; https://www.epicgames.com/help/c-202300000001636/c-202300000001720/will-i-be-banned-if-i-use-third-party-software-with-fortnite-a202300000016487?lang=en-US (snippet, page not fetched) | Low (no overlay evidence). Medium for the Support "cannot say" stance. |
| PUBG Mobile | No official overlay rule or warning. Official help page lists sanctions for "illegal programs (hacks), abnormal gameplay, and app modification". Secondary report of a Tencent notice: unauthorized third-party helper apps can bring a ban; GFX-tool status unclear. Overlay-caused false bans: RUMOUR only. | https://pubgmobile.helpshift.com/hc/en/3-pubg-mobile/faq/275-a-pop-up-message-saying-your-account-will-be-banned-for-violating-regulation-is-shown-on-my-screen ; https://dotesports.com/?p=137075 (secondary) ; RUMOUR: https://217gs.com/news/pubg-mobile-infinix-gt-20-pro-ban | Low for overlays. Medium that third-party apps are in scope. |
| Free Fire | Broadest wording of the six. Abuse Policy (updated 2026-03-09): use of unauthorized programs "not permitted or authorized" can lead to permanent suspension, and "may or may not give you an advantage", including skin MODs. FAQ: "zero-tolerance". Dimmer and overlay apps are not named, so whether they count is UNVERIFIED. Sportskeeda links an "Abnormal activities detected" message to unauthorized third-party apps (secondary; not fetched). | https://ffsupport.garena.com/hc/en-us/articles/4412928339866-Abuse-Policy ; https://ffsupport.garena.com/hc/en-us/articles/4412920960922-Free-Fire-Game-Security-FAQ ; https://www.sportskeeda.com/free-fire/what-there-abnormal-activities-account-error-free-fire (secondary) | High for the wording. Low for overlay applicability. |
| Call of Duty: Mobile | Activision policy (dated 11/06/19) targets unauthorized third-party software used "to gain an unfair advantage, manipulate stats, and/or manipulate game data". Examples: aimbots, wallhacks, trainers, injectors. Peripheral/app clause requires use "for cheating". No overlay mention. Emulator pop-up "Please Prohibit The Use Of Third Party Plugins Or Apps" reported by a secondary site. A newer unified policy (RICOCHET) was seen in snippets only; whether it covers CoD Mobile is UNVERIFIED. | https://support.activision.com/cod-mobile/articles/call-of-duty-mobile-security-enforcement-policy ; https://earlygame.com/call-of-duty/cod-mobile-please-prohibit-the-use-of-third-party-plugins-apps (secondary) | Medium (2019 text). Low for current scope. |
| Genshin Impact | HoYoLAB notice (2020-09-27): sanctions for scripts or third-party software used to "exploit the game" or to gain "artificial in-game progress", plus account trades. No overlay mention. Secondary report of wrongly applied one-month bans for third-party software (RUMOUR-level). The HoYoLAB thread has an unanswered question about global mouse/keyboard hook software; no official reply on the fetched page. | https://www.hoyolab.com/article/18005 ; https://screenrant.com/genshin-impact-rewards-wrongfully-banned-players-return/ (secondary) | Medium (policy). Low for overlays. |
| Instagram | No Instagram-specific overlay behaviour or rule found. Meta's developer policy bans accessing its products by automated means without permission. The "screen overlay detected" message in one user report blocked a storage/photo permission grant; that is an Android OS message, not Instagram. | https://developers.facebook.com/documentation/development/terms-and-policies/automated-data-collection.md ; https://forums.androidcentral.com/ask-question/741265-i-keep-get-screen-overly-detected-when-trying-post-pics-instagram-print.html (snippet, page not fetched) | Low |

GENERAL ANTI-CHEAT / OVERLAY REPORTS
- No source found showing that any of the six titles' anti-cheat checks for overlays. UNVERIFIED.
- RUMOUR: PUBG Mobile false-positive bans on Infinix GT 20 Pro (XArena booster/overlay). 217GS (2026-10-28) relies on one Reddit post. It also claims Tencent "frequently flags background screen-draw tools". No primary source. https://217gs.com/news/pubg-mobile-infinix-gt-20-pro-ban
- RUMOUR / commercial: a vendor article says overlays cause PUBG anti-cheat false positives. Commercially motivated, unverified. https://bittopup.com/id/article/PUBG-Mobile-2025-AntiCheat-71-Cheats-Banned-False-Ban-Fixes
- ANECDOTE: an Android Central user traced a stray overlay over full-screen games to a game-booster app's background services. Shows overlays can appear over games; not a ban. https://forums.androidcentral.com/threads/overlay-issue.1036698/post-7019139 (snippet)
- Genshin black-screen reports found were PC or GPU-load cases, not Android overlay cases. https://insider.razer.com/razer-support-45/razer-cortex-through-xbox-game-bar-darkened-genshin-impact-39986 (PC), https://zentalk.asus.com/en/discussion/37110/screen-blackout-caused-by-high-gpu-usage-genshin-impact

GAMES THAT EXPLICITLY BAN OR WARN ABOUT OVERLAY APPS
- None found across the six titles.
- Closest explicit wording: Free Fire (unauthorized programs, no advantage needed). PUBG Mobile (third-party helper apps, per secondary source).
- CoD Mobile and Genshin require an advantage, exploit, or game-data change in their wording. A plain dimmer does not obviously match that. This is inference from the wording.

PRODUCT IMPLICATIONS (inference from the cited sources; not tested)
- Keep dimmer alpha below 0.8 to avoid blocking taps on Android 12+.
- Going darker than about 80% black under pass-through is not achievable with a single layer, and stacking layers does not help.
- Tell the user to turn the dimmer off while granting other permissions, because Samsung and Verizon report "screen overlay" errors in that case.

GAPS (UNVERIFIED)
- Epic FAQ page: not readable (403 and timeouts); search snippet only.
- Full ToS / anti-cheat docs for Epic, Garena, Activision (2026 policy), HoYoverse and Meta: not read in full.
- Android Central Instagram thread: snippet only (fetch failed).
- Settings.canDrawOverlays: detail not read.
- Whether any of the six titles calls setHideOverlayWindows: unknown.
- No device tests run.

## Göz sağlığı kanıtı
EYE COMFORT EVIDENCE REPORT (English; Turkish app context noted only where relevant)

Scope: searched and opened primary abstracts or pages where marked. Items not confirmed from a source I opened are marked UNVERIFIED. Strength uses strong / moderate / weak / none.

1. SCREEN BRIGHTNESS AND EYE STRAIN
- Lowering screen brightness reduced self-rated eye fatigue after 1 month. 47 completed across 3 arms (brightness, color temperature, control). Brightness arm: -0.82 points, P=0.0007. Other arms: no significant change. Self-reported; blinding not stated | weak | https://djo.harvard.edu/index.php/djo/article/view/790
- In a dark room (<3 lx), lower screen luminance was reported as less fatiguing. n=15 lab study. Between-color comparison not significant (F=1.855, P=0.163) | weak | https://pmc.ncbi.nlm.nih.gov/articles/PMC9185549
- Comfortable LCD luminance rises with ambient light. Suggested evening zone: ambient about 13-62 lx, screen about 21-75 cd/m2. n=33, lab, subjective ratings | weak | https://doi.org/10.3390/app11094108
- Changing wall brightness behind the screen did not significantly change asthenopic symptoms (CRT and LCD, lab). Objective function shifts were noted | weak | https://doi.org/10.1080/10803548.1999.11076438
- Ambient light: 300 lux gave best performance; 0 and 1200 lux were worst; brighter light increased glare complaints. Old thesis, CRT/LCD era | weak | https://vtechworks.lib.vt.edu/items/3f31bcf6-d31f-4e77-a258-be5d4a9b0dd1
- Screen brightness and glare relative to paper are a proposed mechanism for ocular-surface strain. Mechanism only, no outcome trial | moderate (mechanism) | https://research.aston.ac.uk/en/publications/tfos-lifestyle-impact-of-the-digital-environment-on-the-ocular-su/
- "Match screen brightness to room light" is AAO advice. Lab support is mixed | weak (expert advice) | https://www.aao.org/eye-health/tips-prevention/computer-usage
- Dimming below the phone's hardware minimum relieves strain | UNVERIFIED (no study found that tested it)

2. GLARE AND REFLECTIONS
- Glare and reflections are a common complaint. AAO advises a matte screen filter and keeping screens out of window and lamp reflections | weak (expert advice) | https://www.aao.org/eye-health/tips-prevention/computer-usage
- Anti-glare glass filters, field survey of 194 workers: tired-eye complaints fell from 86% to 40%; glare problems fell from 75% to under one-third. Uncontrolled, self-reported; filters donated by the manufacturer | weak | https://news.cornell.edu/stories/1996/04/optical-glass-glare-filters-can-dramatically-improve-computer-related-visual
- Matte or anti-glare filter reduces visual fatigue in a controlled trial | none (no controlled trial found)
- Reflections lower display contrast (optical mechanism). Contrast-ratio figures (11:1 at 20 cd/m2, etc.) and the 20 cd/m2 threshold | UNVERIFIED (search snippets only: a 1982 Canadian report and a patent)

3. BLUE-LIGHT FILTERS
- Blue-light-filtering lenses probably do not reduce short-term computer eye strain. Cochrane: 17 RCTs, 619 adults; no difference in visual fatigue at under 1 week. GRADE low certainty | weak (low certainty; points against use) | https://www.cochrane.org/CD013244/
- Lens blocking 99% of 400-500 nm was no better than a luminance-matched neutral-density filter for symptoms. n=23, 30-min tablet reading | weak (small, single session) | https://pubmed.ncbi.nlm.nih.gov/30570598/
- Blue-light lenses do not appear to be an effective DES strategy (expert review, TFOS 2023) | moderate (consensus review) | https://research.aston.ac.uk/en/publications/tfos-lifestyle-impact-of-the-digital-environment-on-the-ocular-su/
- No evidence that screen light damages eyes; AAO does not recommend blue-light glasses | moderate (expert consensus) | https://aao.org/eye-health/tips-prevention/are-computer-glasses-worth-it
- Sleep benefit of blue-light lenses: inconsistent (3 of 6 trials positive). Do not promise sleep benefit | weak | https://www.cochrane.org/CD013244/
- Harms of blue-light lenses: mild, mostly tied to frames | weak | https://www.cochrane.org/CD013244/

4. BREAKS, BLINKING, DISTANCE
- 20-20-20 rule "effective": uncontrolled before/after, n=29, 2 weeks. Symptoms improved but not kept one week after reminders stopped | weak | https://pubmed.ncbi.nlm.nih.gov/35963776/
- Scheduled 20-second breaks (every 5, 10, 20 or 40 min) during a 40-min tablet task, n=30: no effect on symptoms, reading speed or accuracy. Authors say this is not proof breaks are useless. Master's thesis | weak | https://soar.suny.edu/entities/publication/1e55de5e-9ba8-4fb0-97a8-0f218d83a90a
- The specific 20-20-20 timing is proven effective | none
- Regular breaks, more blinking, and better workplace setup "may help" | weak (expert review) | https://research.aston.ac.uk/en/publications/tfos-lifestyle-impact-of-the-digital-environment-on-the-ocular-su/
- Blink rate falls on screens (AAO: about 5-7/min vs about 15 normal). Reduced blink is the main proposed mechanism | moderate (mechanism review; AAO figures cite unnamed studies) | https://www.aao.org/eye-health/tips-prevention/computer-usage
- Screen-related symptoms are usually temporary; no permanent eye damage | moderate (AAO; review) | https://bmjophth.bmj.com/content/3/1/e000146
- Viewing distance about 20-26 in (about 25 in), gaze slightly downward | weak (expert advice) | https://www.aao.org/eye-health/tips-prevention/computer-usage
- Dark or night mode helps sleep | weak (AAO says "can help", no source given) | https://aao.org/eye-health/tips-prevention/are-computer-glasses-worth-it
- DES prevalence of 50% or more among computer users; estimates vary widely by definition | weak | https://bmjophth.bmj.com/content/3/1/e000146

5. NOTES AND EXCLUSIONS
- Excluded as unreliable: Alibaba "product insights" pages (marketing; one cites a 2020 JAMA review I could not verify) | UNVERIFIED
- Excluded: a 2023 Preventive Medicine DES review and an Iran IJPH display review (search snippets only) | UNVERIFIED
- Cochrane page is dated 2023-08-18 and searched to 22 March 2022. No 2024 version was found
- Ambient darkness alone is not shown to help (0 lux worst in the thesis above) | weak

BOTTOM LINE FOR APP CLAIMS
- Supported, weakly: lowering screen brightness to suit surroundings may feel less tiring.
- Not supported: blue-light filtering as an eye-strain fix; 20-20-20 timing as proven; any medical or sleep benefit; dimming below the hardware minimum.

ABOUT PAGE TEXT (3 sentences)
1. This app places a dark, semi-transparent layer over your screen so you can dim it further than your phone's brightness setting allows.
2. Some small studies suggest lower screen brightness can feel less tiring in dim surroundings, but the evidence is limited.
3. This app is not a medical device and does not treat or prevent any eye condition; if your eyes hurt, stay red, or change, see an eye doctor.