package dev.ekrankarartma.s2

import android.app.NotificationManager
import android.content.Intent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ServiceController

/** Servis yasam dongusu, Kapat alicisi, esitleme ve hizli ayar kutucugu davranislari. */
@RunWith(RobolectricTestRunner::class)
class KarartmaServisiTest {

    private val baglam get() = RuntimeEnvironment.getApplication()
    private val baglantilar = mutableListOf<ServiceController<KarartmaErisilebilirlikServisi>>()

    @Before
    fun hazirla() {
        baglam.getSharedPreferences(KarartmaDurumu.TERCIH_DOSYASI, 0).edit().clear().commit()
        erisilebilirlikServisiniEtkinlestir(baglam)
        bildirimIzniVer(true)
    }

    @After
    fun temizle() {
        for (kontrol in baglantilar) kontrol.destroy()
        baglantilar.clear()
        KarartmaErisilebilirlikServisi.ornek?.onUnbind(null)
        erisilebilirlikServisiniDevreDisiBirak(baglam)
        bildirimIzniVer(false)
    }

    /** Servisi olusturur ve baglar. Test sonunda temizlemek icin listeye ekler. */
    private fun servisBagli(): KarartmaErisilebilirlikServisi {
        val kontrol = Robolectric.buildService(KarartmaErisilebilirlikServisi::class.java).create()
        baglantilar += kontrol
        kontrol.get().baglandi()
        return kontrol.get()
    }

    private fun bildirimSayisi(): Int =
        baglam.getSystemService(NotificationManager::class.java).activeNotifications.size

    @Test
    fun baglandiOncekiAcikTercihiGeriYuklemez() {
        // Surec olmus ya da yeniden baslatilmis gibi: tercih "acik" kalmis.
        KarartmaDurumu.tercihYaz(baglam, true)

        val servis = servisBagli()

        assertFalse(servis.karartmaAcikMi)
        assertFalse(KarartmaDurumu.tercihAcik(baglam))
    }

    @Test
    fun karartmayiAcVeKapatKatmaniVeTercihiGunceller() {
        val servis = servisBagli()

        assertTrue(servis.karartmayiAyarla(true))
        assertTrue(servis.karartmaAcikMi)
        assertTrue(KarartmaDurumu.tercihAcik(baglam))
        assertEquals(1, bildirimSayisi())

        assertFalse(servis.karartmayiAyarla(false))
        assertFalse(servis.karartmaAcikMi)
        assertFalse(KarartmaDurumu.tercihAcik(baglam))
        assertEquals(0, bildirimSayisi())
    }

    @Test
    fun kapatAliciServisBagliyenKatmaniKaldirirVeBildirimiIptalEder() {
        val servis = servisBagli()
        servis.karartmayiAyarla(true)

        KapatAlici().onReceive(baglam, Intent(baglam, KapatAlici::class.java).setAction(KapatAlici.EYLEM))

        assertFalse(servis.karartmaAcikMi)
        assertFalse(KarartmaDurumu.tercihAcik(baglam))
        assertEquals(0, bildirimSayisi())
    }

    @Test
    fun onUnbindKatmaniVeTercihiKapatirVeOrnegiBirakir() {
        val servis = servisBagli()
        servis.karartmayiAyarla(true)

        servis.onUnbind(null)

        assertFalse(servis.karartmaAcikMi)
        assertFalse(KarartmaDurumu.tercihAcik(baglam))
        assertNull(KarartmaErisilebilirlikServisi.ornek)
        assertEquals(0, bildirimSayisi())
    }

    @Test
    fun esitleServisYokkenKalmisBildirimiVeTercihiTemizler() {
        // Surec olmus, bildirim ve tercih kalmis gibi.
        KarartmaDurumu.tercihYaz(baglam, true)
        KarartmaBildirimi.goster(baglam)
        assertEquals(1, bildirimSayisi())

        KarartmaDurumu.esitle(baglam)

        assertFalse(KarartmaDurumu.tercihAcik(baglam))
        assertEquals(0, bildirimSayisi())
    }

    @Test
    fun kutucukServisBagliykenKarartmayiDegistirir() {
        val servis = servisBagli()
        // TileService icin ServiceController.destroy() Robolectric gölgesinde sınıf dönüşüm
        // hatası verir; bu yuzden kontrolcu yok edilmez, yalnizca olusturulur.
        val kutucuk = Robolectric.buildService(KarartmaKutucugu::class.java).create().get()

        kutucuk.onClick()
        assertTrue(servis.karartmaAcikMi)
        assertTrue(KarartmaDurumu.tercihAcik(baglam))

        kutucuk.onClick()
        assertFalse(servis.karartmaAcikMi)
        assertFalse(KarartmaDurumu.tercihAcik(baglam))
    }
}
