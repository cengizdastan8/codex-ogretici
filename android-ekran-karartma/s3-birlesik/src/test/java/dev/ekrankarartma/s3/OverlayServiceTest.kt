package dev.ekrankarartma.s3

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
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
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

/** Katman servisinin çizim davranışı: ekleme, yerinde güncelleme ve izin yokken başlatmama. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OverlayServiceTest {

    private val app = RuntimeEnvironment.getApplication()

    @Before
    fun resetState() {
        app.getSharedPreferences("karartma", Context.MODE_PRIVATE).edit().clear().commit()
        OverlayService.isRunning = false
        OverlayService.isStartPending = false
        ShadowSettings.setCanDrawOverlays(true)
    }

    @Test
    fun acikKatmanRenkVeAlfayiYerindeGunceller() {
        val servis = Robolectric.buildService(OverlayService::class.java).create().get()
        servis.onStartCommand(komut(OverlayService.ACTION_START), 0, 1)
        val katman = requireNotNull(katmanGorunumu(servis)) { "Katman eklenmedi." }
        assertTrue(OverlayService.isRunning)
        assertEquals(Ton.NOTR.renk, (katman.background as ColorDrawable).color)

        OverlayPrefs.setTon(app, Ton.SICAK)
        OverlayPrefs.setYogunluk(app, Yogunluk.GUCLU)
        servis.onStartCommand(komut(OverlayService.ACTION_UPDATE), 0, 2)

        // Aynı görünüm nesnesi kalır; pencere yeniden eklenmez.
        assertTrue(katman === katmanGorunumu(servis))
        assertEquals(Ton.SICAK.renk, (katman.background as ColorDrawable).color)
        val params = katman.layoutParams as WindowManager.LayoutParams
        assertEquals(Yogunluk.GUCLU.alfa, params.alpha, 1e-6f)
    }

    @Test
    fun izinYokkenKatmanEklenmezVeServisKapanir() {
        ShadowSettings.setCanDrawOverlays(false)
        val servis = Robolectric.buildService(OverlayService::class.java).create().get()

        servis.onStartCommand(komut(OverlayService.ACTION_START), 0, 1)

        assertNull(katmanGorunumu(servis))
        assertFalse(OverlayService.isRunning)
        assertFalse(OverlayPrefs.overlayOn(app))
    }

    @Test
    fun yokEdilirkenKatmanKaldirilir() {
        val servis = Robolectric.buildService(OverlayService::class.java).create().get()
        servis.onStartCommand(komut(OverlayService.ACTION_START), 0, 1)
        assertTrue(OverlayService.isRunning)

        servis.onDestroy()

        assertNull(katmanGorunumu(servis))
        assertFalse(OverlayService.isRunning)
        assertFalse(OverlayPrefs.overlayOn(app))
    }

    private fun komut(action: String): Intent = Intent(app, OverlayService::class.java).setAction(action)

    /** Servisin özel [overlayView] alanı. Robolectric pencere yöneticisi görünümleri listelemediği için yansıtma. */
    private fun katmanGorunumu(servis: OverlayService): View? {
        val alan = OverlayService::class.java.getDeclaredField("overlayView")
        alan.isAccessible = true
        return alan.get(servis) as View?
    }
}
