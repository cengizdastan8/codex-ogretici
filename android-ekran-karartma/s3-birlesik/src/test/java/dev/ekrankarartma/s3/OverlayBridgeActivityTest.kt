package dev.ekrankarartma.s3

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowSettings
import java.time.LocalDateTime

/** Kutucuğun kullandığı saydam köprü: açılış sürerken gelen dokunuş kapatma olarak yorumlanır. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OverlayBridgeActivityTest {

    private val app = RuntimeEnvironment.getApplication()

    @Before
    fun resetState() {
        app.getSharedPreferences("karartma", Context.MODE_PRIVATE).edit().clear().commit()
        OverlayService.isRunning = false
        OverlayService.isStartPending = false
        ShadowSettings.setCanDrawOverlays(true)
        ShadowAlarmManager.reset()
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 12, 0) }
    }

    @After
    fun restoreClock() {
        Saat.simdi = { LocalDateTime.now() }
    }

    @Test
    fun bosDurumdaDokunusKarartmayiAcar() {
        Robolectric.buildActivity(OverlayBridgeActivity::class.java).create()

        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun acilisSurerkenIkinciDokunusKapatir() {
        // İlk dokunuş başlatma isteğini gönderdi; servis henüz isRunning yapmadı.
        OverlayService.isStartPending = true
        OverlayPrefs.setOverlayOn(app, true)

        Robolectric.buildActivity(OverlayBridgeActivity::class.java).create()

        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
        assertNull(shadowOf(app).nextStartedService)
        assertFalse(OverlayPrefs.overlayOn(app))
    }

    @Test
    fun calisanKarartmayiKapatir() {
        OverlayService.isRunning = true
        OverlayPrefs.setOverlayOn(app, true)

        Robolectric.buildActivity(OverlayBridgeActivity::class.java).create()

        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
    }
}
