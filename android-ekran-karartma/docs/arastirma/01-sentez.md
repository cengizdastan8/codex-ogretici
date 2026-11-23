# Araştırma sentezi: Android ekran karartma

Tarih: 2026-11-23. Kaynaklar satır içi bağlantılı. Cihaz testi yapılmadı; "UNVERIFIED" işaretli maddeler doğrulanmadı.

Bu rapor beş araştırma raporunun birleşimidir. Cihazda test yapılmadı. Aksi belirtilmedikçe davranışlar kaynak metinlerine veya kod okumasına dayanır. UNVERIFIED, doğrulanmamış anlamına gelir.

## 1. Açık kaynak ve mevcut uygulamalar tablosu

| Uygulama | Repo / kaynak | Lisans | Teknik | Öne çıkan özellik | Ana şikayet |
|---|---|---|---|---|---|
| Red Moon | [LibreShift/red-moon](https://github.com/LibreShift/red-moon) | GPL-3.0-or-later | Overlay (erişilebilirlik > uygulama > sistem tipi); DIM_MAX_ALPHA 0.9; WRITE_SETTINGS ile parlaklık 0; root modu | Gün batımı-doğumu planı, profiller, QS kutucuğu, widget | Android 12'de dokunma kesilmesi ([#309](https://github.com/raatmarien/red-moon/issues/309)); filtrenin kendini açması ([#319](https://github.com/raatmarien/red-moon/issues/319)); bakım durumu (README) |
| code-crusher/dimmer | [code-crusher/dimmer](https://github.com/code-crusher/dimmer) | Lisans dosyası yok, tüm haklar saklı | TYPE_SYSTEM_OVERLAY; targetSdk 25; kaydırıcı en fazla ~%54 karartma (kod) | Ortam ışığı sensörü ile duraklatma | Örneklemde şikayet yok; 2018'den beri commit yok; dokunma davranışı UNVERIFIED |
| MidnightBeam | [MadeinTaly/midnightbeam](https://github.com/MadeinTaly/midnightbeam) | MIT | Erişilebilirlik veya uygulama overlay; WRITE_SECURE_SETTINGS ile servis; kodda %95 karartma; root filtre | Sıcaklık, ön ayarlar (Evening/Night), zamanlama, uyku zamanlayıcısı (deneysel) | Geniş izinler ([F-Droid](https://f-droid.org/packages/dev.midnightbeam/)); WRITE_SECURE_SETTINGS kurulumu |
| Circadian Display | [tomar-mohit/circadian-display](https://github.com/tomar-mohit/circadian-display) | Apache-2.0 | TYPE_APPLICATION_OVERLAY; Android 12+ alfa tavanı 0.76; yerel mod WRITE_SECURE_SETTINGS | Eğri tabanlı sıcaklık ve karartma; arka plan planlayıcı | Yerel mod için ADB ([F-Droid](https://f-droid.org/packages/com.circadiandisplay.app/)) |
| gloomy | [mority/gloomy](https://github.com/mority/gloomy) | MIT | İzin yok; tam ekran Compose aktivitesi; yalnız kendi penceresi | Tek ekran, kaydırma ile parlaklık, renk, QS kutucuğu | Uygulama dışını karartmaz (kapsam sınırı); QS kutucuğu sorunu ([issues](https://github.com/mority/gloomy/issues)) |
| OpenLumen | [SysAdminDoc/OpenLumen](https://github.com/SysAdminDoc/OpenLumen) | GPL-3.0-or-later | 4 sürücü: overlay (~0.8 tavan), SecureSettings (WRITE_SECURE_SETTINGS), 2 root sürücü | Konum tabanlı plan, 4 hazır kutucuk, widget | Teknik kurulum: adb pm grant veya Shizuku (README) |
| Tideo Auto Brightness | [faded-penguin021/Tideo-Auto-Brightness](https://github.com/faded-penguin021/Tideo-Auto-Brightness) | MIT | Otomatik parlaklık; reduce_bright_colors; overlay yok; minSdk 31 | Eşik altında otomatik karartma | WRITE_SECURE_SETTINGS "otomatik verilmez" ([F-Droid](https://f-droid.org/packages/com.tideo.autobrightness/)) |
| Screen & Notifications Dimmer | [ericytsang/app.android.screenfilter](https://github.com/ericytsang/app.android.screenfilter) | UNVERIFIED (okunmadı) | Erişilebilirlik kullanıyor görünüyor (şikayet ve SSS'den çıkarım; kod okunmadı) | Yüksek puan, ~4.61 (aggregator: [chrome-stats](https://chrome-stats.com/d/com.github.ericytsang.screenfilter.app.android)) | Erişilebilirlik izni itirazı ([issues](https://github.com/ericytsang/app.android.screenfilter/issues)); pil optimizasyonu servisi kapatıyor ([SSS](https://ericytsang.github.io/app.android.screenfilter/faq.html)) |
| Lux (com.dimmerpro.app) | [Play](https://play.google.com/store/apps/details?id=com.dimmerpro.app) | Kapalı kaynak, sayılmadı | Ödemeler Adapty; PostHog analitiği ([gizlilik politikası](https://hardikjain826.github.io/app-policies/dimmer-pro/privacy-policy)) | "Reklamsız" (geliştirici iddiası) | Play'den kaldırıldığı iddiası çelişkili ([MTE](https://maketecheasier.com/screen-dimming-android-apps)), UNVERIFIED |
| Night Screen: Ultra Dim Mode | [AppBrain](https://www.appbrain.com/app/night-screen-ultra-dim-mode/com.androxus.screendimmer) | Kapalı (Play) | UNVERIFIED | Çok sayıda mod | Reklam ve premium fiyat (tek yorum, UNVERIFIED) |
| Super Dim | [chrome-stats](https://chrome-stats.com/d/com.tasca.superdim) | Kapalı (Play) | UNVERIFIED | Karartma seviyeleri | Bazı seviyeler ücretli (özet) |

Dışlanan veya okunamayanlar: pariseed/lux ([repo](https://github.com/pariseed/lux), karartıcı değil, 2023'ten beri commit yok); marshmeadow/dimtv ([repo](https://github.com/marshmeadow/dimtv), okunamadı); D4rK7355608/com.d4rk.lowbrightness (okunamadı, lisans UNVERIFIED); crDroid parlaklık modülü (root, okunmadı).

Eksik: Türkiye pazarına özgü hiçbir kaynak yok. Play yorumları ve Reddit/XDA kanıtı alınamadı.

## 2. Kümülatif özellik listesi

Öncelik sütunu ürün sürümlerini gösterir. MVP yalnızca S1 çekirdeğidir. "Sonraki sürüm" S3 kapsamına girer.

| # | Özellik | Kaynak | Öncelik | Not |
|---|---|---|---|---|
| 1 | Tam ekran siyah, dokunma geçirgen katman (TYPE_APPLICATION_OVERLAY + NOT_TOUCHABLE/NOT_FOCUSABLE) | [Red Moon Overlay.kt](https://github.com/LibreShift/red-moon), [Circadian](https://github.com/tomar-mohit/circadian-display), [S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams) | MVP | S1 çekirdeği |
| 2 | Alfa tavanı, 0.8'in altı (ör. 0.75) | [Circadian OverlayOpacityPolicy](https://github.com/tomar-mohit/circadian-display); [MidnightBeam Compat.java](https://github.com/MadeinTaly/midnightbeam) | MVP | 31+ için [getMaximumObscuringOpacityForTouch](https://developer.android.com/sdk/api_diff/31/changes/android.hardware.input.InputManager) okunmalı; yoksa 0.8 altı |
| 3 | Tek büyük aç/kapa düğmesi, tek ekran | [gloomy MainActivity](https://github.com/mority/gloomy) | MVP | Düğme overlay üzerinde olamaz (dokunma geçirgen); uygulama aktivitesinde olmalı |
| 4 | Hızlı ayar kutucuğu (aç/kapa) | [Red Moon TileReceiver](https://github.com/LibreShift/red-moon), [gloomy](https://github.com/mority/gloomy), [TileService](https://developer.android.com/reference/android/service/quicksettings/TileService) | MVP (isteğe bağlı kısayol) | Arka plan FGS başlatma UNVERIFIED; aktivite köprüsü önerilir |
| 5 | Son durumu hatırlama | [gloomy MainActivity.kt:104-106, 208-211](https://github.com/mority/gloomy) | MVP | Yeniden açılışta ayar korunur |
| 6 | Zorunlu FGS bildirimi | [Red Moon](https://github.com/LibreShift/red-moon), [service types](https://developer.android.com/develop/background-work/services/fgs/service-types) | MVP | specialUse |
| 7 | Gizlilik politikası ve izin gerekçesi | [Lux politikası](https://hardikjain826.github.io/app-policies/dimmer-pro/privacy-policy); şikayet: [Red Moon #248](https://github.com/raatmarien/red-moon/issues?page=2) | MVP | Kısa, açık metin |
| 8 | Ücretsiz; tüm seviyeler açık, reklamsız | Şikayetler: [Super Dim](https://chrome-stats.com/d/com.tasca.superdim), [Night Screen](https://www.appbrain.com/app/night-screen-ultra-dim-mode/com.androxus.screendimmer) | MVP (ilke) | Tek tek tanıklık, ama tutarlı tema |
| 9 | Canlı değişim, parlak ekran flaşı yok | [MidnightBeam F-Droid](https://f-droid.org/packages/dev.midnightbeam/) (geliştirici iddiası) | MVP (tasarım ilkesi) | Doğrulanmadı |
| 10 | Sıcak ton seçeneği | [Circadian](https://github.com/tomar-mohit/circadian-display) (amber, en fazla alfa 0.5); [Red Moon](https://github.com/LibreShift/red-moon) profil rengi | Sonraki sürüm (S3) | Çip ile, kaydırma yok |
| 11 | Ön ayarlar (Evening/Night) | [MidnightBeam DimService.java:216-218](https://github.com/MadeinTaly/midnightbeam) | Sonraki sürüm (S3) | Tek ekran içinde |
| 12 | Gün batımı-doğumu planı | [Red Moon LocationUpdateService](https://github.com/LibreShift/red-moon), [Circadian](https://github.com/tomar-mohit/circadian-display) | Sonraki sürüm (S3) | Konum izni ekler |
| 13 | Sabit saat planı (konum istemez) | Kaynak yok, tasarım önerisi | Sonraki sürüm | Kaynaksız; test gerekir |
| 14 | Plan sırasında manuel ayarın korunması | Şikayet: [Red Moon #320, #331](https://github.com/raatmarien/red-moon/issues) | Sonraki sürüm (plan ile birlikte) | Kural önerisi |
| 15 | Açılışta otomatik başlama | [code-crusher manifest satır 37](https://github.com/code-crusher/dimmer) (varsayılan kapalı); [Red Moon](https://github.com/LibreShift/red-moon) | Sonraki sürüm | Kontrolsüz başlama şikayeti ([#316](https://github.com/raatmarien/red-moon/issues/316)) |
| 16 | Bildirimden hızlı kapatma | [Red Moon](https://github.com/LibreShift/red-moon); sorun [#319](https://github.com/raatmarien/red-moon/issues/319) | Sonraki sürüm | Bildirim anahtarı geri dönmemeli |
| 17 | Ana ekran widget'ı | [Red Moon SwitchAppWidgetProvider](https://github.com/LibreShift/red-moon), [OpenLumen](https://github.com/SysAdminDoc/OpenLumen) | Sonraki sürüm | "Widget eklenemiyor" ([#358](https://github.com/raatmarien/red-moon/issues)) |
| 18 | Kaydırıcı ile yoğunluk | [Circadian](https://github.com/tomar-mohit/circadian-display), [MidnightBeam](https://github.com/MadeinTaly/midnightbeam) | Atla | S1 "kaydırma yok"; S3'te çip kullanılır |
| 19 | Ortam ışığı sensörü ile duraklatma | [code-crusher ScreenDimmer.java:138-143](https://github.com/code-crusher/dimmer) | Atla | Tek kaynak, ek karmaşa |
| 20 | Uygulama bazlı duraklatma | [Red Moon CurrentAppMonitor](https://github.com/LibreShift/red-moon) | Atla | Kullanım erişimi ek özel izin |
| 21 | Uyku zamanlayıcısı, webhook, uzak sunucu | [MidnightBeam](https://github.com/MadeinTaly/midnightbeam) | Atla | Deneysel; ağ ve gizlilik yükü |
| 22 | Parlaklık = 0 (WRITE_SETTINGS) | [Red Moon BrightnessManager.kt:42-45, 72](https://github.com/LibreShift/red-moon); [S21](https://developer.android.com/reference/android/provider/Settings.System) | Atla | Etkisi UNVERIFIED; ek izin |
| 23 | Root filtre (SurfaceFlinger) | [Red Moon](https://github.com/LibreShift/red-moon), [OpenLumen](https://github.com/SysAdminDoc/OpenLumen), [MidnightBeam](https://github.com/MadeinTaly/midnightbeam) | Atla | Root gerekir |
| 24 | Extra Dim / reduce_bright_colors sürücüsü | [OpenLumen README](https://github.com/SysAdminDoc/OpenLumen), [Tideo](https://f-droid.org/packages/com.tideo.autobrightness/) | Atla | ADB/Shizuku; "teknik kurulum yok" hedefi ile çelişir |
| 25 | Erişilebilirlik overlay | [Red Moon](https://github.com/LibreShift/red-moon), [MidnightBeam](https://github.com/MadeinTaly/midnightbeam) | Atla (MVP); S2 deneyi | Play politikası riski |

## 3. Yapılmaması gerekenler (şikayetlerden)

- Dokunmayı kesen katman yapılmamalı. Kaynaklar: [Red Moon #309](https://github.com/raatmarien/red-moon/issues/309) (ayrıntılı tek rapor); [F-Droid forum](https://forum.f-droid.org/t/night-light-apps-make-my-touch-screen-unrespinsive/16507) (tek yorum); [Mozilla 1189478](https://bugzilla.mozilla.org/show_bug.cgi?id=1189478) (2015, kapatıldı); [#293](https://github.com/raatmarien/red-moon/issues?page=2) (yalnız başlık).
- Zamanlama veya bildirim, kullanıcının manuel kapatmasını geri çevirmemeli. Kaynaklar: [#319](https://github.com/raatmarien/red-moon/issues/319), [#320, #331, #346](https://github.com/raatmarien/red-moon/issues) (başlık ve kısa açıklamalar).
- Kontrolsüz otomatik başlatma yapılmamalı. Kaynaklar: [#316](https://github.com/raatmarien/red-moon/issues/316); [Screen Light Dimmer](https://chrome-stats.com/d/com.brigdimmer.scrlghfilter) (snippet, UNVERIFIED).
- Yeniden başlatmada çökme riski. Kaynak: [#306](https://github.com/raatmarien/red-moon/issues/306) (düzeltildiği söylenmiş, hâlâ açık).
- Teknik kurulum istenmemeli. Kaynaklar: [#309](https://github.com/raatmarien/red-moon/issues/309) (adb komutu); [#343](https://github.com/raatmarien/red-moon/issues) (açık); [Tideo F-Droid](https://f-droid.org/packages/com.tideo.autobrightness/).
- Erişilebilirlik izni gerekçesiz istenmemeli, tekrar tekrar sorulmamalı. Kaynaklar: [issues](https://github.com/ericytsang/app.android.screenfilter/issues); [SSS](https://ericytsang.github.io/app.android.screenfilter/faq.html).
- Reklam ve ödemeli karartma seviyeleri konulmamalı. Kaynaklar: [Night Screen](https://www.appbrain.com/app/night-screen-ultra-dim-mode/com.androxus.screendimmer) (tek yorum, UNVERIFIED); [Screen Light Dimmer](https://chrome-stats.com/d/com.brigdimmer.scrlghfilter) (snippet); [Super Dim](https://chrome-stats.com/d/com.tasca.superdim) (özet).
- Gizlilik politikası olmadan izin istenmemeli. Kaynaklar: [Red Moon #248](https://github.com/raatmarien/red-moon/issues?page=2); [MidnightBeam izin listesi](https://f-droid.org/packages/dev.midnightbeam/) (listeleme gerçeği, şikayet değil).
- Bakımsız bırakılmamalı. Kaynak: [Red Moon #281](https://github.com/raatmarien/red-moon/issues?page=2); [code-crusher commit tarihi](https://github.com/code-crusher/dimmer).
- Sistem ayarlarına müdahale edilmemeli. Kaynak: [Red Moon #335](https://github.com/raatmarien/red-moon/issues/335) (tek rapor, root).
- İzin verme sırasında karartıcı açık bırakılmamalı. Kaynak: [Samsung destek](https://www.samsung.com/us/support/troubleshooting/TSG10004868/) (rapor kaynağı; sayfa bu analizde doğrudan okunmadı).

Eksik: Play yorumları okunamadığı için şikayet kapsamı sınırlı.

## 4. Teknik kısıtlar ve izin akışı

### 4.1 Platform kısıtları

- minSdk 26: TYPE_APPLICATION_OVERLAY API 26 ile geldi ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)). Rakipler 26 kullanıyor ([MidnightBeam](https://github.com/MadeinTaly/midnightbeam), [Circadian](https://github.com/tomar-mohit/circadian-display)).
- targetSdk 36+: Play'de 31 Ağustos 2026'dan itibaren zorunlu ([S18](https://developer.android.com/google/play/requirements/target-sdk)). compileSdk 37: Android 17, 16 Haziran 2026'da stabil ([S15](https://developer.android.com/blog/posts/android-17-is-here), [S16](https://developer.android.com/about/versions/17/behavior-changes-17)).
- Android 12+ (API 31): dokunma geçirgenliğinde alfa tavanı vardır ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams), [S3](https://developer.android.com/topic/security/risks/tapjacking)). Tavan cihaza göre değişir.
- Android 13+ (API 33): POST_NOTIFICATIONS çalışma zamanı izni ([S11](https://developer.android.com/develop/ui/views/notifications/notification-permission)). Reddedilirse FGS bildirimi çekmecede gizlenir, Görev Yöneticisinde görünür ([S12](https://developer.android.com/about/versions/13/behavior-changes-13)).
- Android 14+ (API 34): foregroundServiceType zorunlu; yoksa MissingForegroundServiceTypeException ([S7](https://developer.android.com/about/versions/14/changes/fgs-types-required)).
- Android 15+ (API 35): SAW izni olsa da görünür TYPE_APPLICATION_OVERLAY yoksa arka plandan FGS başlatılamaz. BOOT_COMPLETED alıcıları dataSync, camera, mediaPlayback, phoneCall, mediaProjection, microphone FGS başlatamaz. specialUse bu listede değildir ([S10](https://developer.android.com/about/versions/15/behavior-changes-15)).
- Android 16/17: raporun taradığı listelerde overlay, FGS türü, yan yükleme veya bildirim değişikliği bulunmadı ([S13](https://developer.android.com/about/versions/16/behavior-changes-all), [S14](https://developer.android.com/about/versions/16/behavior-changes-16), [S16](https://developer.android.com/about/versions/17/behavior-changes-17)). Android 17 BAL sıkılaştırması için MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE önerilir ([S16](https://developer.android.com/about/versions/17/behavior-changes-17)).
- Yan yükleme: Google geliştirici doğrulaması 30 Eylül 2026'da Brezilya, Endonezya, Singapur ve Tayland'da başlıyor. Küresel plan 2027. Doğrulanmamış geliştiriciler gelişmiş akışla yükleyebilir ([S17](https://developer.android.com/developer-verification/guides)).
- Sürümler: Gradle 9.9.2 ([S38](https://gradle.org/releases/)); Kotlin 2.4.21, sayfa tarihleri tutarsız ([S39](https://kotlinlang.org/docs/releases.html)); AGP 9.5.0, 9.6 yalnız alfa, kararlılık UNVERIFIED ([S36](https://developer.android.com/build/releases/gradle-plugin), [S37](https://developer.android.com/build/releases/agp-9-6-0-release-notes)); androidx.core 1.19.1 ([S40](https://developer.android.com/jetpack/androidx/releases/core)); androidx.activity 1.13.0 ([S41](https://developer.android.com/jetpack/androidx/releases/activity)). Platform sayfası bayat ve API 37 içermiyor ([S42](https://developer.android.com/tools/releases/platforms)).
- Ayar anahtarları: Settings.Secure resmi sayfasında reduce_bright_colors yok ([S25](https://developer.android.com/reference/android/provider/Settings.Secure)). AOSP XML farklı anahtarlar kullanıyor ([S26](https://android.googlesource.com/platform/packages/apps/Settings/+/879babf246/res/xml/reduce_bright_colors_settings.xml)). Anahtarlara bağımlı tasarım yapılmamalı.
- Extra Dim: kullanıcı ayarıdır, uygulama denetleyemez. Pixel 9 ve öncesinde Erişilebilirlik altında, Pixel 10+ parlaklık kaydırıcısında. Sürüm belirtilmemiş ([S22](https://support.google.com/accessibility/android/answer/11183305?hl=en)).
- Parlaklık: belgelenmiş hiçbir API donanım alt sınırının altına inmez ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams), [S21](https://developer.android.com/reference/android/provider/Settings.System)). Karartma için tek belgelenmiş yol overlay'dir.

### 4.2 İzinler

| İzin (tam ad) | İşlevi | Nasıl istenir (sürüm) | Kullanıcıyı götürdüğü ekran | Karar |
|---|---|---|---|---|
| android.permission.SYSTEM_ALERT_WINDOW ("Diğer uygulamaların üzerinde göster") | TYPE_APPLICATION_OVERLAY çizmek ([S2](https://developer.android.com/reference/android/Manifest.permission), [S3](https://developer.android.com/topic/security/risks/tapjacking)) | Manifest + ACTION_MANAGE_OVERLAY_PERMISSION; Settings.canDrawOverlays ile kontrol ([S4](https://developer.android.com/reference/android/provider/Settings)). API 23-29: package: verisiyle uygulamanın kendi satırı açılabilir. API 30+: genel liste açılır, kullanıcı uygulamayı seçer ([S5](https://developer.android.com/preview/privacy/permissions)) | API 23-29: uygulama satırı. API 30+: genel liste; uygulama içinde "uygulamayı bulun, anahtarı açın" adımı. Türkçe etiketler doğrulanmadı | MVP |
| android.permission.POST_NOTIFICATIONS | FGS bildirimini göstermek | API 33+ çalışma zamanı izni; FGS'den önce bağlamda istenir ([S11](https://developer.android.com/develop/ui/views/notifications/notification-permission)) | Sistem izin diyaloğu | MVP |
| android.permission.FOREGROUND_SERVICE_SPECIAL_USE | FGS türü specialUse ([S8](https://developer.android.com/develop/background-work/services/fgs/service-types)) | Normal izin; kurulumda verilir, geri alınamaz (rapor). PROPERTY_SPECIAL_USE_FGS_SUBTYPE metni Play'de incelenir ([S7](https://developer.android.com/about/versions/14/changes/fgs-types-required)) | Yok | MVP |
| android.permission.FOREGROUND_SERVICE | FGS başlatmak | Manifest'te; rakiplerde var ([MidnightBeam manifest](https://github.com/MadeinTaly/midnightbeam)). Resmi alıntı bu raporda yok | Yok | MVP (kaynak eksik) |
| android.permission.WRITE_SETTINGS | Sistem parlaklığı yazmak | Manifest + ACTION_MANAGE_WRITE_SETTINGS; canWrite() API 23+ ([S21](https://developer.android.com/reference/android/provider/Settings.System)) | Sistem izin ekranı (etiket UNVERIFIED) | Atla |
| android.permission.WRITE_SECURE_SETTINGS | Güvenli ayarları yazmak | Üçüncü taraflar için değil ([S2](https://developer.android.com/reference/android/Manifest.permission)). Uygulama istemez; adb pm grant, Shizuku veya root ([OpenLumen](https://github.com/SysAdminDoc/OpenLumen), [Tideo](https://f-droid.org/packages/com.tideo.autobrightness/)) | Yok (komut satırı) | Atla |
| Erişilebilirlik servisi (BIND_ACCESSIBILITY_SERVICE, sistem bağlar) | TYPE_ACCESSIBILITY_OVERLAY, değer 2032, API 22+ ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)) | Servis, intent filter, XML ([S28](https://developer.android.com/guide/topics/ui/accessibility/service)). Kullanıcı açar ([S30](https://codelabs.developers.google.com/codelabs/developing-android-a11y-service), özet) | Erişilebilirlik > uygulama > aç. Yan yüklemede "Kısıtlı ayarlara izin ver" (ikincil, UNVERIFIED) | S2 deneyi; Play riski |
| RECEIVE_BOOT_COMPLETED | Açılışta başlatma | Çalışma zamanı izni yok; rakip manifestlerinde ([Circadian](https://github.com/tomar-mohit/circadian-display)) | Yok | Sonraki sürüm |
| ACCESS_COARSE_LOCATION | Gün batımı hesabı ([Red Moon](https://github.com/LibreShift/red-moon)) | Çalışma zamanı izni; akış bu raporda yok (UNVERIFIED) | Konum diyaloğu | Sonraki sürüm |
| SCHEDULE_EXACT_ALARM | Tam zamanlı alarm ([Red Moon](https://github.com/LibreShift/red-moon), [Circadian](https://github.com/tomar-mohit/circadian-display)) | Özel izin; akış araştırılmadı (UNVERIFIED) | Bilinmiyor | Sonraki sürüm |

Kısıtlı ayarlar (yalnız ikincil kaynak, UNVERIFIED): Yan yüklenen uygulamalarda erişilebilirlik ve bazı izinler engellenebilir. Kullanıcı "Kısıtlı ayarlara izin ver" seçeneğini uygulama bilgisi ekranında bulur ([S32](https://www.androidauthority.com/android-13-sideloading-apps-restrictions-3161162), [S33](https://cleanbrowsing.org/support/mobile/disable-restricted-settings-android), [S34](https://www.kaspersky.com/blog/android-restricted-settings/49991/), [S35](https://www.androidauthority.com/android-15-restricted-settings-sideloading-3481098)). Resmi Android 13 sayfaları bunu içermiyor ([S31](https://developer.android.com/about/versions/13/behavior-changes-all)). SAW için de geçerli olup olmadığı doğrulanmadı.

Eksik: Resmi kısıtlı ayar metni, WRITE_SECURE_SETTINGS koruma seviyesi ve konum/alarm izin akışları bulunamadı.

## 5. Üç senaryo

Not: Tek büyük düğme, overlay içinde olamaz. Overlay dokunma geçirgen olmalıdır. Düğme uygulama aktivitesinde veya hızlı ayar kutucuğunda yer alır. Bu bir çelişkidir ve tasarımda çözülmelidir.

### S1 Overlay-tek-düğme

(a) Yaklaşım: Uygulama aktivitesinde "İzin ver" ve "Başlat" düğmeleri. TYPE_APPLICATION_OVERLAY tam ekran siyah, FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE, alfa 0.75 veya altı, specialUse FGS ile çalışır ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)). Kutucuk: onClick, startActivityAndCollapse ile görünür şeffaf aktivite açar, aktivite FGS'yi başlatır ([S19](https://developer.android.com/develop/ui/views/quicksettings-tiles), [S20](https://developer.android.com/reference/android/service/quicksettings/TileService)). Yalnız aktivite üzerinden başlatma, Android 15 kuralıyla uyumludur ([S10](https://developer.android.com/about/versions/15/behavior-changes-15)).

(b) İzinler: SYSTEM_ALERT_WINDOW (zorunlu), POST_NOTIFICATIONS (önerilir, Android 13+), FOREGROUND_SERVICE_SPECIAL_USE (otomatik). Kısıtlı ayar gerekmez.

(c) Kullanıcı akışı ve dokunma sayısı (tahmini, cihaza göre değişir, test edilmedi):
- İlk kurulum: uygulamayı aç (1), "İzin ver" (1), Ayarlar'da uygulamayı bulup anahtarı aç (2-4), geri dön (1), bildirim izni (1), "Başlat" (1). Toplam yaklaşık 7-9.
- Sonraki kullanım, uygulamadan: aç (1), düğme (1). Toplam 2.
- Kutucukla: QS panelini aşağı çekmek bir sistem kaydırmasıdır (1 jest), kutucuğa dokunmak 1. Kutucuk ilk sayfada değilse düzenleme gerekir. Android 13+ için requestAddTileService kullanılabilir ([S19](https://developer.android.com/develop/ui/views/quicksettings-tiles)).

(d) Riskler:
- Alfa 0.8 ve üzeri dokunmayı keser. Sınır belirsiz ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams) "eşit ve altı" diyor, [S3](https://developer.android.com/topic/security/risks/tapjacking) ">= 0.8" diyor).
- Tek overlay en fazla ~%80 karartır, dokunma geçirgen kalırken.
- Kutucuğun arka plan FGS başlatması UNVERIFIED. Muafiyet listesinde kutucuk yok ([S9](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)).
- Sistem overlay'i gizleyebilir veya taşıyabilir ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)).
- Samsung izin verme sırasında "Screen overlay detected" hatası verebilir ([Samsung](https://www.samsung.com/us/support/troubleshooting/TSG10004868/)).
- Oyunlarda hesap riski için kaynaklı kanıt yok. PUBG söylentisi tek Reddit gönderisine dayanıyor ([217GS](https://217gs.com/news/pubg-mobile-infinix-gt-20-pro-ban)). Free Fire'ın politikası geniş ifadelidir ([Abuse Policy](https://ffsupport.garena.com/hc/en-us/articles/4412928339866-Abuse-Policy)), overlay kapsamı UNVERIFIED.
- Play, specialUse metnini inceler ([S7](https://developer.android.com/about/versions/14/changes/fgs-types-required)).
- Bildirim reddedilirse FGS bildirimi çekmecede görünmez ([S12](https://developer.android.com/about/versions/13/behavior-changes-13)).

(e) Test kriterleri:
1. Android 12+ cihazda alfa 0.75 ile ana ekran, Chrome, ayarlar ve bir oyunda dokunmalar geçiyor.
2. Alfa 0.80 ve 0.85 ile aynı testler, sınır kaydedilir.
3. Kutucuk ile arka plandan başlatma Android 14, 15 ve 16/17 cihazlarda çalışıyor.
4. Uygulama kapatılınca FGS ve overlay davranışı tutarlı.
5. Bildirim reddedildiğinde Görev Yöneticisinde FGS görünüyor.
6. Android 15'te görünür aktiviteden başlatma başarılı.
7. Yeniden başlatmadan sonra durum beklenen şekilde (açık kalmaz).
8. İlk kurulum 9 dokunmayı aşmıyor (hedef).

### S2 Erişilebilirlik-katmanı

(a) Yaklaşım: AccessibilityService ile TYPE_ACCESSIBILITY_OVERLAY. Bu pencereler güvenilirdir ve alfa 0.8 tavanı uygulanmaz ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)). Kodda tam siyah mümkündür (MidnightBeam maxAlpha=1), dokunma davranışı UNVERIFIED. canRetrieveWindowContent="false" ayarlanmalıdır ([Red Moon XML](https://github.com/LibreShift/red-moon)).

(b) İzinler: BIND_ACCESSIBILITY_SERVICE (sistem bağlar). SYSTEM_ALERT_WINDOW gerekmez. Overlay yalnız servis bağlıyken görünür ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)).

(c) Kullanıcı akışı ve dokunma sayısı (tahmini, test edilmedi):
- Uygulamayı aç (1), "Erişilebilirliği aç" (1), Ayarlar > Erişilebilirlik > uygulama (2-3; menü adları cihaza göre değişir, [S30](https://codelabs.developers.google.com/codelabs/developing-android-a11y-service)), anahtar (1), onay (1), geri (1-2), "Başlat" (1). Toplam yaklaşık 8-10.
- Yan yüklemede kısıtlı ayar (ikincil, UNVERIFIED): uygulama bilgisi > menü > "Kısıtlı ayarlara izin ver" ek 3-5 dokunma.

(d) Riskler:
- Play erişilebilirlik politikası: "yalnız yardımcı teknoloji" amacı ([S28](https://developer.android.com/guide/topics/ui/accessibility/service), [S29](https://developer.android.com/guide/topics/ui/accessibility/views/service)). Karartma bu amaca uymuyor. Play onay riski yüksek. Politika metni bu raporda okunmadı.
- Gizlilik itirazı: "Dimmer needs accessibility access" ([issues](https://github.com/ericytsang/app.android.screenfilter/issues)).
- Pil optimizasyonu servisi kapatabilir ([SSS](https://ericytsang.github.io/app.android.screenfilter/faq.html)).
- Kısıtlı ayar adımı ikincil kaynaklıdır (UNVERIFIED).
- OEM menü adları farklıdır.
- Alfa 1.0'da dokunma geçişi UNVERIFIED.

(e) Test kriterleri:
1. Yan yüklü ve Play kurulumlu Android 13+ cihazda servis etkinleştirme akışı kaydedilir.
2. Alfa 1.0 ile oyun ve Chrome'da dokunma geçişi test edilir.
3. Pil optimizasyonu sonrası servisin kapanıp kapanmadığı ölçülür.
4. Manifestte canRetrieveWindowContent="false" doğrulanır.
5. Play politika ön değerlendirmesi yapılır (UNVERIFIED).
6. Kullanıcı kısıtlı ayar adımlarını tek başına tamamlayabiliyor.

### S3 Birleşik-ürün

(a) Yaklaşım: S1 temeli, tek ekran. Büyük aç/kapa düğmesi, ton çipleri (sıcak/nötr/mat; ton adları ürün kararı), zaman planı anahtarı, durum yazısı. Son ayar kaydedilir. Kutucuk isteğe bağlı kısayoldur. Kaynaklar: [Circadian](https://github.com/tomar-mohit/circadian-display) (sıcaklık), [Red Moon](https://github.com/LibreShift/red-moon) (zamanlama), [gloomy](https://github.com/mority/gloomy) (tek ekran).

(b) İzinler: S1'deki izinler. Gün batımı seçilirse konum izni (akış UNVERIFIED). Önerilmez: erişilebilirlik, WRITE_SECURE_SETTINGS, WRITE_SETTINGS.

(c) Kullanıcı akışı: S1 ile aynı. Ton değiştirme 1 dokunma. Zaman planını açma 1 dokunma. Gün batımı seçilirse konum diyaloğu 1-2 dokunma. Günlük kullanım: 2 dokunma (aç/kapa) ve gerekirse 1 ton dokunuşu.

(d) Riskler:
- Zamanlama manuel ayarı geri çevirebilir ([#320, #331](https://github.com/raatmarien/red-moon/issues)).
- Tek ekranda çok seçenek karmaşa yaratır.
- Konum izni güveni düşürebilir.
- "Sıcak ton" göz sağlığı iddiası taşımamalı. Kanıt sadece mavi ışık filtrelerinin etkisizliğini gösteriyor ([Cochrane](https://www.cochrane.org/CD013244/)).
- Reklam ve ödeme baskısı.

(e) Test kriterleri:
1. S1 testlerinin tamamı geçer.
2. Manuel kapatma, sonraki plan sınırına kadar korunur (kural önerisi; kaynak: [#320](https://github.com/raatmarien/red-moon/issues)).
3. Saat dilimi değişiminde plan doğru çalışır.
4. Konum reddedilirse sabit saat yedeği devreye girer (öneri, kaynaksız).
5. Tüm kontroller ilk ekranda görünür, kaydırma gerekmez.
6. Yeniden başlatmadan sonra plan yeniden kurulur.

## 6. Doğrulanmamış veya zayıf kanıtlı iddialar

- 0.8 sınırında dokunma davranışı belirsiz ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams), [S3](https://developer.android.com/topic/security/risks/tapjacking); eski blog: [Medium](https://medium.com/androiddevelopers/untrusted-touch-events-2c0e0b9c374c), 403, yalnız snippet).
- Kutucuk tıklamasının arka plan FGS başlatma izni: UNVERIFIED ([S9](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)).
- Parlaklık 0.0 cihazın donanım minimumudur: UNVERIFIED ([S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)).
- Red Moon parlaklık 0 yolunun gerçek etkisi: UNVERIFIED (kod okundu, cihaz yok).
- Red Moon alfa 0.9'da dokunma: UNVERIFIED.
- code-crusher dokunma davranışı: kod okumasından çıkarım, UNVERIFIED.
- Erişilebilirlik overlay'de tam siyah dokunma geçirgenliği: UNVERIFIED ([MidnightBeam](https://github.com/MadeinTaly/midnightbeam) maxAlpha=1).
- Engel logcat metni "Untrusted touch due to occlusion": yalnız ikincil snippet ([Medium](https://medium.com/androiddevelopers/untrusted-touch-events-2c0e0b9c374c)).
- reduce_bright_colors anahtar adları: AOSP'de farklı, gizli sabitler yalnız ayna kaynakta ([S27](https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/b6697b2a32d6e77eaa1786dd850b27e5a04e8ae9)). Kullanılmamalı.
- Android 13/15'te kısıtlı ayarların SAW'ı da kapsaması: ikincil kaynaklar ([S32](https://www.androidauthority.com/android-13-sideloading-apps-restrictions-3161162), [S35](https://www.androidauthority.com/android-15-restricted-settings-sideloading-3481098)); resmi metin yok.
- Extra Dim'in hangi Android sürümünde geldiği: sürüm belirtilmemiş ([S22](https://support.google.com/accessibility/android/answer/11183305?hl=en)).
- AGP 9.5.1 ve 9.6.0 kararlılığı: UNVERIFIED ([S36](https://developer.android.com/build/releases/gradle-plugin)).
- Kotlin 2.4.21 tarihi: sayfa tutarsız ([S39](https://kotlinlang.org/docs/releases.html)).
- PUBG overlay nedeniyle hesap yasağı: söylenti ([217GS](https://217gs.com/news/pubg-mobile-infinix-gt-20-pro-ban), tek Reddit gönderisi; [bittopup](https://bittopup.com/id/article/PUBG-Mobile-2025-AntiCheat-71-Cheats-Banned-False-Ban-Fixes), ticari kaynak).
- Oyunların setHideOverlayWindows kullanıp kullanmadığı: UNVERIFIED ([tapjacking](https://developer.android.com/topic/security/risks/tapjacking)).
- Şikayetlerin çoğu başlık veya snippet: [Samsung S21](https://community.samsung.com/uk/conversations/other-galaxy-s-series/overlay-apps-not-working-on-s21-ultra-running-oneui-4-0-and/eu_4411200) (gövde kesik), ASUS, Mobizen, Fairphone, Night Screen yorumu, Brightness Dimmer Pro ve Screen Light Dimmer (snippet).
- "Minimum altı karartma" kullanıcı övgüsü: tek forum snippet ([Fairphone](https://forum.fairphone.com/t/screen-brightness/73185)).
- Screen & Notifications Dimmer 4.61 puanı: aggregator ([chrome-stats](https://chrome-stats.com/d/com.github.ericytsang.screenfilter.app.android)).
- Red Moon kaldırma sonrası cihazın bozulması: tek rapor ([#335](https://github.com/raatmarien/red-moon/issues/335)).
- Lux'ın Play'den kaldırılması: çelişkili ([MTE](https://maketecheasier.com/screen-dimming-android-apps)).
- Android 8'de Twilight status bar kaybı: UNVERIFIED ([PhoneArena](https://www.phonearena.com/news/Android-O-breaks-apps-like-Twilight-and-Status-that-draw-over-the-system-UI_id92983)).
- Reddit ve XDA kanıtı: bulunamadı.
- Samsung/Verizon "screen overlay" hatası: [Samsung](https://www.samsung.com/us/support/troubleshooting/TSG10004868/) rapora göre yüksek güven. [Verizon](https://www.verizon.com/support/knowledge-base-209672) yalnız snippet.

## 7. Göz sağlığı konusunda uygulamanın dürüstçe söyleyebilecekleri

Söyleyebilecekleri (zayıf kanıt, ölçülü dil):
- Ekran parlaklığını ortama uydurmak daha az yorucu hissettirebilir: [Harvard DJO](https://djo.harvard.edu/index.php/djo/article/view/790) (47 kişi, öz bildirim, zayıf); [PMC9185549](https://pmc.ncbi.nlm.nih.gov/articles/PMC9185549) (15 kişi, loş oda, zayıf); [MDPI](https://doi.org/10.3390/app11094108) (33 kişi, laboratuvar, zayıf); [AAO](https://www.aao.org/eye-health/tips-prevention/computer-usage) (uzman tavsiyesi).
- Ekran kaynaklı belirtiler genellikle geçicidir (orta düzey, derleme): [BMJ Ophthalmology](https://bmjophth.bmj.com/content/3/1/e000146).
- Ekranda göz kırpma sıklığı düşer (orta düzey mekanizma): [AAO](https://www.aao.org/eye-health/tips-prevention/computer-usage).

Söyleyemeyecekleri:
- Mavi ışık filtresi göz yorgunluğunu azaltır: hayır. Düşük güvenli Cochrane derlemesi fark bulamadı ([Cochrane CD013244](https://www.cochrane.org/CD013244/)). Uzman konsensüsü de etkisiz diyor ([TFOS](https://research.aston.ac.uk/en/publications/tfos-lifestyle-impact-of-the-digital-environment-on-the-ocular-su/)).
- Uyku faydası: tutarsız ([Cochrane](https://www.cochrane.org/CD013244/)). Vaat edilmemeli.
- Donanım alt sınırının altında karartmanın yorgunluğu azalttığı: UNVERIFIED, bu konuda çalışma yok.
- 20-20-20 kuralının zamanlamasının kanıtlandığı: hayır. Kanıt zayıf ([PubMed 35963776](https://pubmed.ncbi.nlm.nih.gov/35963776/)); bir tezde etkisi görülmedi ([SUNY](https://soar.suny.edu/entities/publication/1e55de5e-9ba8-4fb0-97a8-0f218d83a90a)).
- Ekran ışığının gözü kalıcı olarak zarar verdiği: kanıt yok ([AAO](https://aao.org/eye-health/tips-prevention/are-computer-glasses-worth-it)).
- Göz hastalığını tedavi ettiği veya önlediği: hayır, tıbbi iddia yasaktır.

Önerilen "hakkında" metni (Türkçe, ürün metni):
1. "Bu uygulama ekranınızın üzerine koyu, yarı saydam bir katman yerleştirerek ekranı cihazın parlaklık ayarının daha altına indirmenize yardımcı olur." (Yöntem açıklaması; kaynak: [S1](https://developer.android.com/reference/android/view/WindowManager.LayoutParams).)
2. "Küçük çalışmalar, ekran parlaklığını ortama uydurmanın loş ortamlarda daha az yorucu hissettirebileceğini gösteriyor; ancak kanıt sınırlıdır ve bu uygulamanın göz sağlığına etkisi kanıtlanmamıştır." (Kaynak: yukarıdaki çalışmalar.)
3. "Bu uygulama tıbbi bir cihaz değildir; göz hastalığını tedavi etmez veya önlemez. Gözleriniz ağrıyor, kızarıyor ya da görmenizde değişiklik varsa bir göz doktoruna başvurun." (Güvenlik notu; kaynak gerektirmez, tıbbi iddia içermez.)

## Eksik bölümler ve sınırlar

- Türkiye pazarına özgü kullanıcı verisi yok. Play yorumları okunamadı. Reddit ve XDA'da kanıt bulunamadı.
- Oyun kullanım şartlarının (Epic, Garena tam metni, Activision 2026, HoYoverse, Meta) tamamı okunmadı.
- Cihaz testi yapılmadı. Tüm dokunma, izin ve arka plan davranışları UNVERIFIED.
- Resmi kısıtlı ayar metni, WRITE_SECURE_SETTINGS koruma seviyesi, reduce_bright_colors resmi belgesi, erişilebilirlik Play politikası metni, konum ve SCHEDULE_EXACT_ALARM akışları bulunamadı.
- Screen & Notifications Dimmer, Low Brightness ve DimTV lisans ve kod bilgisi alınamadı. Lux'ın kaynak kodu yok.
- Android 16 ve 17 değişiklik listeleri yalnız özetle okundu.