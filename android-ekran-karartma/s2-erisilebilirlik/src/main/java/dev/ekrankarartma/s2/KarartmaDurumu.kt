package dev.ekrankarartma.s2

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log

internal enum class EkranDurumu {
    /** Erisilebilirlik servisi ayarlarda etkin degil. */
    SERVIS_KAPALI,

    /** Ayarlarda etkin ama servis baglanmamis (static bayrak false). */
    SERVIS_BAGLANAMADI,

    /** Servis bagli, karartma kapali. */
    KAPALI,

    /** Servis bagli, karartma acik. */
    ACIK
}

/** Saf fonksiyon: ekran durumunu uc girdiden belirler. */
internal fun ekranDurumuHesapla(
    servisEtkin: Boolean,
    servisBagli: Boolean,
    karartmaAcik: Boolean
): EkranDurumu = when {
    !servisEtkin -> EkranDurumu.SERVIS_KAPALI
    !servisBagli -> EkranDurumu.SERVIS_BAGLANAMADI
    karartmaAcik -> EkranDurumu.ACIK
    else -> EkranDurumu.KAPALI
}

/** Kalici tercih (SharedPreferences "karartma") ve durum sorgulari. */
internal object KarartmaDurumu {
    const val TERCIH_DOSYASI = "karartma"
    private const val ANAHTAR_ACIK = "acik"
    private const val ETIKET = "KarartmaDurumu"

    fun tercihAcik(baglam: Context): Boolean =
        baglam.getSharedPreferences(TERCIH_DOSYASI, Context.MODE_PRIVATE)
            .getBoolean(ANAHTAR_ACIK, false)

    /**
     * Tercihi senkron yazar (commit). apply() asenkrondur: surec bu sirada olurse "kapali"
     * degeri kaybolabilir ve karartma bir sonraki baglantida yanlislikla geri gelebilir.
     * Lint'in ApplySharedPref uyarisi bilerek susturulur: yazma ana iste, tek ve kucuk bir degerdir.
     */
    @SuppressLint("ApplySharedPref")
    fun tercihYaz(baglam: Context, acik: Boolean) {
        baglam.getSharedPreferences(TERCIH_DOSYASI, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(ANAHTAR_ACIK, acik)
            .commit()
    }

    /** Servis ayarlarda bu uygulama icin etkin mi? */
    fun servisEtkinMi(baglam: Context): Boolean {
        val liste = Settings.Secure.getString(
            baglam.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val kimlik = ComponentName(baglam, KarartmaErisilebilirlikServisi::class.java).flattenToString()
        return liste.split(':').any { it.equals(kimlik, ignoreCase = true) }
    }

    /** Gercek durum: servis bagli ve karartma tercihi acik. */
    fun aktifMi(baglam: Context): Boolean =
        KarartmaErisilebilirlikServisi.bagli && tercihAcik(baglam)

    fun ekranDurumu(baglam: Context): EkranDurumu = ekranDurumuHesapla(
        servisEtkin = servisEtkinMi(baglam),
        servisBagli = KarartmaErisilebilirlikServisi.bagli,
        karartmaAcik = tercihAcik(baglam)
    )

    /**
     * Servis bagli degilken "acik" tercih kalamaz. Tercih duzeltilir ve surec olmus olabilecegi icin
     * kalmis bildirim ile kutucuk da kapali duruma getirilir.
     */
    fun esitle(baglam: Context) {
        if (tercihAcik(baglam) && !KarartmaErisilebilirlikServisi.bagli) {
            tercihYaz(baglam, false)
            KarartmaBildirimi.iptal(baglam)
            kutucuguGuncelle(baglam)
        }
    }

    /** Hizli ayar kutucugunu yeniden okutur. Sistem desteklemezse sessizce gecilir. */
    fun kutucuguGuncelle(baglam: Context) {
        try {
            TileService.requestListeningState(
                baglam,
                ComponentName(baglam, KarartmaKutucugu::class.java)
            )
        } catch (e: RuntimeException) {
            Log.w(ETIKET, "Hizli ayar kutucugu guncellenemedi", e)
        }
    }
}
