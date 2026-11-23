package dev.ekrankarartma.s2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build

/**
 * Normal bildirim (FGS degil). Karartma aciken "Kapat" eylemi sunar; bu, katmanin
 * uygulamanin kendi dugmesini ortmesi durumunda ana kapatma yollarindan biridir.
 * POST_NOTIFICATIONS yoksa bildirim gosterilmez, karartma yine calisir.
 */
internal object KarartmaBildirimi {
    private const val KANAL_KIMLIGI = "karartma_durumu"
    private const val BILDIRIM_KIMLIGI = 2002
    private const val ISTEK_KODU_KAPAT = 21
    private const val ISTEK_KODU_UYGULAMA = 22
    private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
    private val BAYRAKLAR = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    /**
     * Bildirim gosterilebilir mi. API 33+ icin POST_NOTIFICATIONS verilmis olmali.
     * Izin yoksa bildirim gelmez; ekran bunu kullaniciya bildirmek icin bu degeri kullanir.
     */
    fun izinVar(baglam: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            baglam.checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Bildirimi gosterir ya da ayni kimlikteki eskisinin yerine koyar (idempotent). */
    fun goster(baglam: Context) {
        if (!izinVar(baglam)) return
        val yonetici = baglam.getSystemService(NotificationManager::class.java) ?: return
        kanalHazirla(yonetici, baglam)

        val uygulamaNiyeti = PendingIntent.getActivity(
            baglam,
            ISTEK_KODU_UYGULAMA,
            Intent(baglam, MainActivity::class.java),
            BAYRAKLAR
        )
        val kapatNiyeti = PendingIntent.getBroadcast(
            baglam,
            ISTEK_KODU_KAPAT,
            Intent(baglam, KapatAlici::class.java).setAction(KapatAlici.EYLEM),
            BAYRAKLAR
        )
        val kapatEylemi = Notification.Action.Builder(
            Icon.createWithResource(baglam, R.drawable.ic_karartma),
            baglam.getString(R.string.bildirim_kapat),
            kapatNiyeti
        ).build()

        val bildirim = Notification.Builder(baglam, KANAL_KIMLIGI)
            .setSmallIcon(R.drawable.ic_karartma)
            .setContentTitle(baglam.getString(R.string.bildirim_baslik))
            .setContentText(baglam.getString(R.string.bildirim_metin))
            .setContentIntent(uygulamaNiyeti)
            .addAction(kapatEylemi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .build()

        try {
            yonetici.notify(BILDIRIM_KIMLIGI, bildirim)
        } catch (e: SecurityException) {
            // Bildirim izni sistem tarafinda geri alindiysa sessizce gec.
        }
    }

    fun iptal(baglam: Context) {
        baglam.getSystemService(NotificationManager::class.java)?.cancel(BILDIRIM_KIMLIGI)
    }

    private fun kanalHazirla(yonetici: NotificationManager, baglam: Context) {
        if (yonetici.getNotificationChannel(KANAL_KIMLIGI) != null) return
        val kanal = NotificationChannel(
            KANAL_KIMLIGI,
            baglam.getString(R.string.bildirim_kanali),
            NotificationManager.IMPORTANCE_LOW
        )
        yonetici.createNotificationChannel(kanal)
    }
}
