package dev.ekrankarartma.s2

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

/**
 * Tek ekran: baslik, durum yazisi ve tek birincil dugme.
 * Dugmenin islevi duruma gore degisir: erisilebilirlik ayarina git, karart, ya da karartmayi kapat.
 */
class MainActivity : Activity() {

    private lateinit var durumYazisi: TextView
    private lateinit var adimYazisi: TextView
    private lateinit var anaDugme: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        durumYazisi = findViewById(R.id.durum)
        adimYazisi = findViewById(R.id.adim)
        anaDugme = findViewById(R.id.ana_dugme)
        anaDugme.setOnClickListener { anaDugmeyeBasildi() }
        kenarlariSistemCubuklarinaGore(findViewById(R.id.kok))
        bildirimIzniIste()
    }

    override fun onResume() {
        super.onResume()
        // Erisilebilirlik ayarlarindan dondugunde ve her acilista gercek durumu yeniden okur.
        durumuYenile()
        // Bildirim izni ayarlardan sonradan verilmis olabilir. Karartma aciksa bildirimi simdi ekle.
        if (KarartmaDurumu.aktifMi(this)) KarartmaBildirimi.goster(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Hizli ayar paneli veya bildirim, uygulama ustteyken karartmayi degistirebilir.
        // Odak geri geldiginde etiket gercek duruma gore yenilenir.
        if (hasFocus) durumuYenile()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Izin sonradan verildiyse ve karartma aciksa bildirimi simdi goster.
        // Reddedilirse karartma yine calisir; yalnizca "Kapat" bildirimi olmaz.
        if (requestCode == IZIN_ISTEK_KODU && KarartmaDurumu.aktifMi(this)) {
            KarartmaBildirimi.goster(this)
        }
    }

    /** Bildirim izni (API 33+) ilk baslatmada bir kez istenir. */
    private fun bildirimIzniIste() {
        if (Build.VERSION.SDK_INT < 33) return
        val tercihler = getSharedPreferences(KarartmaDurumu.TERCIH_DOSYASI, MODE_PRIVATE)
        if (tercihler.getBoolean(ANAHTAR_IZIN_ISTENDI, false)) return
        tercihler.edit().putBoolean(ANAHTAR_IZIN_ISTENDI, true).apply()
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), IZIN_ISTEK_KODU)
        }
    }

    /**
     * Android 15+ (targetSdk 35+) pencereyi kenardan kenara cizer: ust durum cubugu ve alt gezinme
     * cubugu icerigin altina girer. Bu yuzden sabit dolgu yerine sistem cubugu yuksekligi eklenir.
     * Daha eski surumlerde pencere cubuklarin altinda basladigi icin dolgu XML'deki gibi kalir.
     */
    private fun kenarlariSistemCubuklarinaGore(kok: View) {
        if (Build.VERSION.SDK_INT < 35) return
        val yatay = resources.getDimensionPixelSize(R.dimen.yatay_bosluk)
        val dikey = resources.getDimensionPixelSize(R.dimen.dikey_bosluk)
        kok.setOnApplyWindowInsetsListener { gorunum, insets ->
            val cubuklar = insets.getInsets(WindowInsets.Type.systemBars())
            gorunum.setPadding(
                yatay + cubuklar.left,
                dikey + cubuklar.top,
                yatay + cubuklar.right,
                dikey + cubuklar.bottom
            )
            insets
        }
    }

    private fun durumuYenile() {
        // Servis bagli degilse "acik" tercih kalmaz; arayuz ile gercek durum ayni olur.
        KarartmaDurumu.esitle(this)
        val durum = KarartmaDurumu.ekranDurumu(this)

        durumYazisi.setText(
            when (durum) {
                EkranDurumu.SERVIS_KAPALI -> R.string.durum_servis_kapali
                EkranDurumu.SERVIS_BAGLANAMADI -> R.string.durum_servis_baglanamadi
                EkranDurumu.KAPALI -> R.string.durum_kapali
                EkranDurumu.ACIK -> R.string.durum_acik
            }
        )
        val servisAyarGerekli =
            durum == EkranDurumu.SERVIS_KAPALI || durum == EkranDurumu.SERVIS_BAGLANAMADI
        // Karartma acikken bildirim izni yoksa "Kapat" bildirimi cikmaz; bunu ekranda soyle.
        val bildirimEksik = durum == EkranDurumu.ACIK && !KarartmaBildirimi.izinVar(this)
        adimYazisi.setText(if (bildirimEksik) R.string.bildirim_izni_uyari else R.string.adim_yazisi)
        adimYazisi.visibility = if (servisAyarGerekli || bildirimEksik) View.VISIBLE else View.GONE
        anaDugme.setText(
            when (durum) {
                EkranDurumu.SERVIS_KAPALI,
                EkranDurumu.SERVIS_BAGLANAMADI -> R.string.dugme_erisilebilirlik
                EkranDurumu.KAPALI -> R.string.dugme_karart
                EkranDurumu.ACIK -> R.string.dugme_karartmayi_kapat
            }
        )
    }

    private fun anaDugmeyeBasildi() {
        when (KarartmaDurumu.ekranDurumu(this)) {
            EkranDurumu.SERVIS_KAPALI,
            EkranDurumu.SERVIS_BAGLANAMADI -> erisilebilirlikAyarlariniAc()
            EkranDurumu.KAPALI -> karartmayiDegistir(acik = true)
            EkranDurumu.ACIK -> karartmayiDegistir(acik = false)
        }
        durumuYenile()
    }

    private fun karartmayiDegistir(acik: Boolean) {
        val gercekAcik = KarartmaErisilebilirlikServisi.ornek?.karartmayiAyarla(acik) ?: false
        if (acik && !gercekAcik) {
            Toast.makeText(this, R.string.toast_karartma_baslatilamadi, Toast.LENGTH_SHORT).show()
        }
    }

    private fun erisilebilirlikAyarlariniAc() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_ayarlar_acilamadi, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val IZIN_ISTEK_KODU = 2
        const val ANAHTAR_IZIN_ISTENDI = "bildirim_izni_istendi"
    }
}
