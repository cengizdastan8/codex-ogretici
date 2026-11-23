package dev.ekrankarartma.s2

import android.content.Intent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class KarartmaDurumuTest {

    private val baglam get() = RuntimeEnvironment.getApplication()

    @Before
    fun hazirla() {
        baglam.getSharedPreferences(KarartmaDurumu.TERCIH_DOSYASI, 0).edit().clear().commit()
    }

    @After
    fun temizle() {
        KarartmaErisilebilirlikServisi.ornek?.onUnbind(null)
    }

    @Test
    fun ekranDurumuTablosu() {
        assertEquals(EkranDurumu.SERVIS_KAPALI, ekranDurumuHesapla(false, false, false))
        assertEquals(EkranDurumu.SERVIS_BAGLANAMADI, ekranDurumuHesapla(true, false, false))
        assertEquals(EkranDurumu.SERVIS_BAGLANAMADI, ekranDurumuHesapla(true, false, true))
        assertEquals(EkranDurumu.KAPALI, ekranDurumuHesapla(true, true, false))
        assertEquals(EkranDurumu.ACIK, ekranDurumuHesapla(true, true, true))
    }

    @Test
    fun kapatAliciTercihiKapatir() {
        KarartmaDurumu.tercihYaz(baglam, true)

        KapatAlici().onReceive(baglam, Intent(baglam, KapatAlici::class.java).setAction(KapatAlici.EYLEM))

        assertFalse(KarartmaDurumu.tercihAcik(baglam))
    }

    @Test
    fun servisBagliDegilkenAktifDegil() {
        KarartmaDurumu.tercihYaz(baglam, true)
        assertFalse(KarartmaDurumu.aktifMi(baglam))
        assertTrue(KarartmaDurumu.tercihAcik(baglam))
    }
}
