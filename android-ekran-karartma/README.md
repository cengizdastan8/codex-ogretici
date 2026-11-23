# Ekran Karartma: üç Android senaryosu

Telefon ekranını, cihazın en düşük parlaklık ayarının da altına inecek şekilde yarı saydam bir siyah katmanla karartan üç ayrı uygulama. Üçünde de: tek ekran, kaydırma yok, internet izni yok, reklam ve veri toplama yok, Türkçe arayüz.

> **Önemli:** Bu uygulamalar telefonda henüz test edilmedi. Yalnızca derleme ve bilgisayar üzerinde otomatik testler yapıldı. Oyunlarda ve Instagram'da gerçekten nasıl davrandıklarını sen denemelisin.

## Hangisini kuracağım?

| Senaryo | Ne farkı var | Ne zaman |
|---|---|---|
| **S1: Overlay tek düğme** (`s1-overlay`) | En basit yol. "Diğer uygulamaların üzerinde göster" izni ister. Karartma en fazla %75. | Önce bunu dene. |
| **S2: Erişilebilirlik katmanı** (`s2-erisilebilirlik`) | "Diğer uygulamaların üzerinde göster" izni istemez, erişilebilirlik servisi kullanır. Karartma %95 (deneysel). | S1'de dokunma sorunu olursa ya da daha koyu istersen. |
| **S3: Birleşik ürün** (`s3-birlesik`) | S1'in her şeyi + ton (Nötr / Sıcak / Mat), yoğunluk (Hafif / Orta / Güçlü) ve gece planı (22:00–07:00). | Günlük kullanım için en çok özellik. |

## Kurulum

1. APK dosyasını telefona indirin ve dokunun.
2. Android "bilinmeyen kaynak" uyarısı verirse, bu seferlik izin verin.
3. Uygulamayı açın ve ekrandaki **1. adımı** izleyin.

### S1 ve S3: "Diğer uygulamaların üzerinde göster"

- **1. İzin ver** düğmesine basınca Android'in "Diğer uygulamaların üzerinde göster" listesi açılır.
- Android 11 ve üzeri sürümlerde bu liste doğrudan uygulamanın satırını açmaz, genel listeyi açar. Listede **Ekran Karartma** (S3'te **Ekran Karartma S3**) adını bulun, anahtarı açın, geri dönün.
- Bildirim izni sorulursa **İzin ver** deyin. Vermezseniz karartma yine çalışır, ama bildirimdeki "Kapat" düğmesi görünmez.

### S2: Erişilebilirlik

- **1. Erişilebilirliği aç** düğmesine basın. Açılan listede **Ekran Karartma**'yı bulun, açın, onaylayın, geri dönün.
- Android 13 ve üzerinde yan yüklenen uygulamalarda "Kısıtlı ayarlar" engeli çıkabilir. Bu bilgi ikincil kaynaklara dayanıyor, cihazda doğrulanmadı.
- Pil tasarrufu bu servisi durdurabilir. Durursa karartma kapanır.

## Nasıl kullanılır?

- **Ana düğme:** Karart / Karartmayı kapat.
- **Kapatma yolları:** Ana ekrandaki düğme, bildirimdeki **Kapat**, hızlı ayar kutucuğu (**Ekran Karartma**). Kutucuğu hızlı ayar panelinde görmüyorsanız, paneli açıp kalem simgesinden ekleyebilirsiniz.
- **S2'de:** Karartma açıkken uygulamanın kendi düğmesi de karanlık katmanın altında kalır. Bu yüzden kapatmak için bildirimi ya da kutucuğu kullanın.
- **S3'te:** Ton ve yoğunluk seçimi anında uygulanır. Gece planını açarsanız karartma 22:00'de açılır, 07:00'de kapanır. Saatleri düğmelerden değiştirebilirsiniz. Plan için **"Alarmlar ve hatırlatıcılar"** izni gerekir. Plan aktifken durum çubuğunda alarm simgesi görünebilir.

## Sınırlar (dürüst liste)

- **Parlama:** Karartma ekranın yaydığı ışığı azaltır. Cam yüzeyindeki yansımayı (parlamayı) azaltmaz.
- **Dokunma tavanı:** S1 ve S3'te karartma %75'i geçmez. Android 12 ve üzerinde, tam siyah katman dokunmaları alttaki uygulamaya geçirmez.
- **Göz sağlığı:** Bu uygulama tıbbi bir ürün değildir ve göz hastalığını tedavi etmez. Araştırmada, parlaklık ve mavi ışık için kanıtın zayıf olduğu görüldü. Ayrıntılar: `docs/arastirma/01-sentez.md`.
- **Cihazda test edilmedi:** Otomatik testler (96 test, Robolectric) geçti. Gerçek telefondaki dokunma, izin akışı, oyun uyumluluğu ve plan zamanlaması doğrulanmadı.
- **S3 planı:** Arka plandan servis başlatma davranışı, Android'in resmi referans sayfasına dayanıyor (`setAlarmClock` bölümü), ama resmi "istisna listesinde" bu maddeye rastlanmadı. Cihazda gerçekten 22:00'de açılıp açılmadığını kontrol edin.

## Geliştirici notları

Derleme (Gradle 9.9.2, AGP 9.5.2, compileSdk 36, minSdk 26, targetSdk 36):

```
cd android-ekran-karartma
./gradlew :s1-overlay:assembleDebug :s2-erisilebilirlik:assembleDebug :s3-birlesik:assembleDebug
./gradlew :s1-overlay:testDebugUnitTest :s2-erisilebilirlik:testDebugUnitTest :s3-birlesik:testDebugUnitTest
```

APK'lar `build/outputs/apk/debug/` altında oluşur; repoya eklenmez (`.gitignore`).

Son test sonucu (2026-11-23): 96 test, 0 hata, 1 atlandı. Atlanan test, S2'de font ölçeği 2.0 olduğunda metnin 568 dp sınırını aştığını gösteriyor (bilinen sorun, `@Ignore` ile işaretli). Lint'te hata yok; yalnızca hedef SDK ve yedekleme uyarıları var.

Araştırma: `docs/arastirma/01-sentez.md` (sentez), `docs/arastirma/02-ham-raporlar.md` (ham bulgular ve kaynaklar).
