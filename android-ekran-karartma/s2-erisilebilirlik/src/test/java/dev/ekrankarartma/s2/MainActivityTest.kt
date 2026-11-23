package dev.ekrankarartma.s2

import android.app.NotificationManager
import android.graphics.Insets
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.widget.AbsListView
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class MainActivityTest {

    private val baglam get() = RuntimeEnvironment.getApplication()

    @Before
    fun hazirla() {
        baglam.getSharedPreferences(KarartmaDurumu.TERCIH_DOSYASI, 0).edit().clear().commit()
        erisilebilirlikServisiniDevreDisiBirak(baglam)
        bildirimIzniVer(false)
    }

    @After
    fun temizle() {
        KarartmaErisilebilirlikServisi.ornek?.onUnbind(null)
        erisilebilirlikServisiniDevreDisiBirak(baglam)
        bildirimIzniVer(false)
    }

    private fun kontrolcu(): ActivityController<MainActivity> =
        Robolectric.buildActivity(MainActivity::class.java).setup()

    private fun aktivite() = kontrolcu().get()

    /** Erisilebilirlik servisini etkin ve bagli yapar. Cagiran destroy() ile kapatir. */
    private fun servisBagli(): ServiceController<KarartmaErisilebilirlikServisi> {
        erisilebilirlikServisiniEtkinlestir(baglam)
        val kontrol = Robolectric.buildService(KarartmaErisilebilirlikServisi::class.java).create()
        kontrol.get().baglandi()
        return kontrol
    }

    private fun bildirimSayisi(): Int =
        baglam.getSystemService(NotificationManager::class.java).activeNotifications.size

    @Test
    fun tekBirincilDugmeVarVeKaydirmaYok() {
        val activity = aktivite()
        val kok = activity.findViewById<View>(R.id.kok)
        val gorunumler = tumGorunumler(kok)

        assertEquals(1, gorunumler.count { it is Button })
        assertTrue(gorunumler.none { it is ScrollView || it is HorizontalScrollView || it is AbsListView })
    }

    @Test
    fun erisilebilirlikKapaliyken_dugmeAyarlariAcar() {
        val activity = aktivite()
        val dugme = activity.findViewById<Button>(R.id.ana_dugme)

        assertEquals(activity.getString(R.string.dugme_erisilebilirlik), dugme.text.toString())
        dugme.performClick()

        val niyet = org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_ACCESSIBILITY_SETTINGS, niyet.action)
    }

    @Test
    fun erisilebilirlikAcikkenVeBagliyken_dugmeKarartmayiDegistirir() {
        val kontrol = servisBagli()
        try {
            val activity = aktivite()
            val dugme = activity.findViewById<Button>(R.id.ana_dugme)
            assertEquals(activity.getString(R.string.dugme_karart), dugme.text.toString())

            dugme.performClick()
            assertTrue(KarartmaDurumu.tercihAcik(baglam))
            assertEquals(activity.getString(R.string.dugme_karartmayi_kapat), dugme.text.toString())

            dugme.performClick()
            assertFalse(KarartmaDurumu.tercihAcik(baglam))
            assertEquals(activity.getString(R.string.dugme_karart), dugme.text.toString())
        } finally {
            kontrol.destroy()
        }
    }

    @Test
    fun servisBagliDegilkenAcikTercihiKalmaz() {
        erisilebilirlikServisiniEtkinlestir(baglam)
        KarartmaDurumu.tercihYaz(baglam, true)

        val activity = aktivite()
        val durum = activity.findViewById<TextView>(R.id.durum)

        assertFalse(KarartmaDurumu.tercihAcik(baglam))
        assertEquals(activity.getString(R.string.durum_servis_baglanamadi), durum.text.toString())
    }

    @Test
    fun karartmaAcikVeBildirimIzniYoksaUyariGosterir() {
        val kontrol = servisBagli()
        try {
            assertTrue(kontrol.get().karartmayiAyarla(true))
            val activity = aktivite()
            val adim = activity.findViewById<TextView>(R.id.adim)

            assertEquals(View.VISIBLE, adim.visibility)
            assertEquals(activity.getString(R.string.bildirim_izni_uyari), adim.text.toString())
        } finally {
            kontrol.destroy()
        }
    }

    @Test
    fun izinSonradanVerilirseOnResumeBildirimiEkler() {
        val kontrol = servisBagli()
        try {
            assertTrue(kontrol.get().karartmayiAyarla(true))
            val kontrolcu = kontrolcu()
            // Izin yokken acilan karartma icin bildirim gelmez.
            assertEquals(0, bildirimSayisi())

            // Kullanici izni sistem ayarlarindan sonradan verir, uygulamaya geri doner.
            bildirimIzniVer(true)
            kontrolcu.pause().resume()

            assertEquals(1, bildirimSayisi())
        } finally {
            kontrol.destroy()
        }
    }

    @Test
    fun odakGeriGelinceEtiketGercekDurumaGoreYenilenir() {
        val kontrol = servisBagli()
        try {
            val activity = aktivite()
            val dugme = activity.findViewById<Button>(R.id.ana_dugme)
            assertEquals(activity.getString(R.string.dugme_karart), dugme.text.toString())

            // Hizli ayar kutucugu gibi uygulama disi bir degisiklik.
            kontrol.get().karartmayiAyarla(true)
            activity.onWindowFocusChanged(true)

            assertEquals(activity.getString(R.string.dugme_karartmayi_kapat), dugme.text.toString())
        } finally {
            kontrol.destroy()
        }
    }

    @Test
    fun sistemCubuklariAndroid15TeDolguyaEklenir() {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 35)
        val activity = aktivite()
        val kok = activity.findViewById<View>(R.id.kok)
        val yatayPx = activity.resources.getDimensionPixelSize(R.dimen.yatay_bosluk)
        val dikeyPx = activity.resources.getDimensionPixelSize(R.dimen.dikey_bosluk)

        val cubuklar = WindowInsets.Builder()
            .setInsets(WindowInsets.Type.systemBars(), Insets.of(7, 30, 9, 40))
            .build()
        kok.dispatchApplyWindowInsets(cubuklar)

        assertEquals(yatayPx + 7, kok.paddingLeft)
        assertEquals(dikeyPx + 30, kok.paddingTop)
        assertEquals(yatayPx + 9, kok.paddingRight)
        assertEquals(dikeyPx + 40, kok.paddingBottom)
    }
}
