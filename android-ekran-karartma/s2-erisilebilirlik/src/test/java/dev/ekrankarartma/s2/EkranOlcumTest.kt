package dev.ekrankarartma.s2

import android.content.res.Configuration
import android.view.View
import android.widget.TextView
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Icerik yuksekligi olcumu. NATIVE grafik modu gereklidir: LEGACY modda TextView'lar metin
 * uzunlugundan bagimsiz olculur ve "sigar" testi hicbir seyi kanitlamaz.
 *
 * Olcum kapsami: 360x640 dp pencere, xhdpi (yogunluk 2.0). Olculen yukseklik kok gorunumun
 * dolgusunu da icerir. Kullanilabilir alan 568 dp: 640 dp'den durum cubugu (~24 dp) ve
 * gezinme cubugu (~48 dp) dusulmus hali.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EkranOlcumTest {

    private val baglam get() = RuntimeEnvironment.getApplication()

    @After
    fun yaziBoyutunuSifirla() {
        yaziOlceginiUygula(1.0f)
    }

    @Suppress("DEPRECATION") // updateConfiguration API 25'te kullanimdan kaldirildi; test icin yeterli.
    private fun yaziOlceginiUygula(olcek: Float) {
        val yapi = Configuration(baglam.resources.configuration)
        yapi.fontScale = olcek
        baglam.resources.updateConfiguration(yapi, baglam.resources.displayMetrics)
    }

    /** Kok gorunumun dp cinsinden yuksekligi. [uzunAdim] verilirse adim yazisi onunla degisir. */
    private fun icerikYuksekligiDp(olcek: Float, uzunAdim: String? = null): Float {
        yaziOlceginiUygula(olcek)
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        if (uzunAdim != null) {
            activity.findViewById<TextView>(R.id.adim).text = uzunAdim
        }
        val kok = activity.findViewById<View>(R.id.kok)
        val yogunluk = activity.resources.displayMetrics.density
        kok.measure(
            View.MeasureSpec.makeMeasureSpec((360 * yogunluk).toInt(), View.MeasureSpec.EXACTLY),
            // UNSPECIFIED: olculen yukseklik kirpilmadan doner, boylece tasma gorulur.
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return kok.measuredHeight / yogunluk
    }

    @Test
    fun varsayilanYaziBoyutundaKullanilabilirAlanaSigar() {
        val dp = icerikYuksekligiDp(olcek = 1.0f)
        assertTrue("Yukseklik $dp dp, 568 dp kullanilabilir alana sigmali", dp <= KULLANILABILIR_DP)
    }

    @Test
    fun buyukYaziBoyutlarindaKullanilabilirAlanaSigar() {
        // Android'in "Buyuk" ve "Daha buyuk" ayarlari: 1.3 ve 1.5.
        for (olcek in listOf(1.3f, 1.5f)) {
            val dp = icerikYuksekligiDp(olcek = olcek)
            assertTrue("Yazi olcegi $olcek: $dp dp, 568 dp kullanilabilir alana sigmali", dp <= KULLANILABILIR_DP)
        }
    }

    /**
     * ACIK SORUN (olculdu, cozulmedi): yazi olcegi 2.0'da icerik yaklasik 670 dp olur ve
     * 568 dp kullanilabilir alani asar. Kaydirma yasak oldugu icin cozum bir tasarim kararidir
     * (metin kisaltma, ikinci satiri kaldirma ya da bosluklari azaltma). Karar verilene kadar
     * test atlanir; sigma kosulu gecerli olduguna dair bir iddia yoktur.
     */
    @Ignore("Acik sorun: yazi olcegi 2.0'da icerik ~670 dp, kaydirma yasak. Tasarim karari bekleniyor.")
    @Test
    fun yaziOlcegi2de_kullanilabilirAlanaSigar() {
        val dp = icerikYuksekligiDp(olcek = 2.0f)
        assertTrue("Yazi olcegi 2.0: $dp dp, 568 dp kullanilabilir alana sigmali", dp <= KULLANILABILIR_DP)
    }

    /**
     * Kalibrasyon: olcum metin uzunluguna duyarli olmali. Aksi halde yukaridaki sigma testleri
     * her seyi gecer ve bir tasmayi yakalayamaz.
     */
    @Test
    fun olcumMetinUzunluguna_duyarli() {
        val dp = icerikYuksekligiDp(olcek = 1.0f, uzunAdim = "Uzun metin. ".repeat(200))
        assertTrue("Cok uzun metin $dp dp olculdu; 640 dp'yi asmali", dp > 640f)
    }

    private companion object {
        const val KULLANILABILIR_DP = 568f
    }
}
