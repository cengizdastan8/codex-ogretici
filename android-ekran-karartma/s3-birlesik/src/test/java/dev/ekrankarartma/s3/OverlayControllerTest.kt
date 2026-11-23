package dev.ekrankarartma.s3

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowSettings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Plan sınırları, açılış davranışı, alarm izni ve manuel kural (servis kuyruğu üzerinden). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OverlayControllerTest {

    private val app = RuntimeEnvironment.getApplication()
    private val alarmManager: AlarmManager
        get() = app.getSystemService(AlarmManager::class.java)

    @Before
    fun resetState() {
        app.getSharedPreferences("karartma", Context.MODE_PRIVATE).edit().clear().commit()
        OverlayService.isRunning = false
        OverlayService.isStartPending = false
        ShadowSettings.setCanDrawOverlays(true)
        ShadowAlarmManager.reset()
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 23, 0) }
    }

    @After
    fun restoreClock() {
        Saat.simdi = { LocalDateTime.now() }
    }

    private fun sonrakiServis(): String? = shadowOf(app).nextStartedService?.action

    private fun millis(zaman: LocalDateTime): Long =
        zaman.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun planAyarla(baslangic: LocalTime = LocalTime.of(22, 0), bitis: LocalTime = LocalTime.of(7, 0)) {
        OverlayPrefs.setPlanPenceresi(app, baslangic, bitis)
        OverlayPrefs.setPlanAcik(app, true)
    }

    @Test
    fun planAcildiginda_pencereIcindeKarartmaAcilirVeAlarmSabahiGosterir() {
        OverlayController.planSet(app, true)

        assertEquals(OverlayService.ACTION_START, sonrakiServis())
        assertEquals(millis(LocalDateTime.of(2026, 11, 24, 7, 0)), alarmManager.nextAlarmClock.triggerTime)
        assertFalse(OverlayPrefs.planBekliyor(app))
    }

    @Test
    fun planBaslangicSinirindaServisiBaslatirVeSonrakiSinirKurar() {
        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 21, 0) }
        OverlayController.planSet(app, true)
        // 21:00 pencere dışı: servis başlamamalı.
        assertNull(sonrakiServis())

        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 22, 0) }
        OverlayController.onPlanBoundary(app)

        assertEquals(OverlayService.ACTION_START, sonrakiServis())
        assertEquals(millis(LocalDateTime.of(2026, 11, 24, 7, 0)), alarmManager.nextAlarmClock.triggerTime)
    }

    @Test
    fun planBitisSinirindaServisiDurdurur() {
        planAyarla()
        OverlayService.isRunning = true
        OverlayPrefs.setOverlayOn(app, true)
        Saat.simdi = { LocalDateTime.of(2026, 11, 24, 7, 0) }

        OverlayController.onPlanBoundary(app)

        assertEquals(OverlayService.ACTION_STOP, sonrakiServis())
        assertEquals(millis(LocalDateTime.of(2026, 11, 24, 22, 0)), alarmManager.nextAlarmClock.triggerTime)
    }

    @Test
    fun durdurmaIstegiGonderilemezseKayitliDurumAcikKalir() {
        OverlayService.isRunning = true
        OverlayPrefs.setOverlayOn(app, true)
        // Arka plan kısıtı simülasyonu: servise her startService çağrısı reddedilir.
        val reddeden = object : ContextWrapper(app) {
            override fun startService(service: Intent): ComponentName? =
                throw IllegalStateException("arka plan başlatma kısıtı")
        }

        OverlayController.applyNow(reddeden, false)

        assertTrue(OverlayService.isRunning)
        assertTrue(OverlayPrefs.overlayOn(app))
    }

    @Test
    fun durdurmaBasariliyseKayitliDurumKapanir() {
        OverlayService.isRunning = true
        OverlayPrefs.setOverlayOn(app, true)

        OverlayController.applyNow(app, false)

        assertEquals(OverlayService.ACTION_STOP, sonrakiServis())
        assertFalse(OverlayPrefs.overlayOn(app))
    }

    @Test
    fun acilistaYalnizcaPlaniYenidenKurarKarartmayiAcmaz() {
        planAyarla()
        BootReceiver().onReceive(app, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertNull(sonrakiServis())
        assertNotNull(alarmManager.nextAlarmClock)
    }

    @Test
    fun planKapatilinca_alarmIptalEdilir() {
        planAyarla()
        OverlayController.syncPlan(app)
        assertNotNull(alarmManager.nextAlarmClock)

        OverlayController.planSet(app, false)

        assertNull(alarmManager.nextAlarmClock)
        assertFalse(OverlayPrefs.planBekliyor(app))
    }

    @Test
    fun alarmIzniYoksaPlanBekler_vePencereyiUygulamaz() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)

        OverlayController.planSet(app, true)

        assertTrue(OverlayPrefs.planBekliyor(app))
        assertNull(sonrakiServis())
    }

    @Test
    fun izinVerilinceBekleyenPlanHemenUygulanir() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        OverlayController.planSet(app, true)
        assertTrue(OverlayPrefs.planBekliyor(app))

        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        OverlayController.onAppResumed(app)

        assertFalse(OverlayPrefs.planBekliyor(app))
        assertEquals(OverlayService.ACTION_START, sonrakiServis())
    }

    @Test
    fun pencereDegisinceManuelKuralSifirlanir() {
        planAyarla()
        OverlayController.manualSet(app, false)
        assertNotNull(OverlayPrefs.gecersizKilma(app))

        val kabul = OverlayController.planWindowChanged(app, LocalTime.of(9, 0), LocalTime.of(17, 0))

        assertTrue(kabul)
        assertNull(OverlayPrefs.gecersizKilma(app))
    }

    @Test
    fun ayniBaslangicVeBitisKabulEdilmez() {
        planAyarla()

        val kabul = OverlayController.planWindowChanged(app, LocalTime.of(22, 0), LocalTime.of(22, 0))

        assertFalse(kabul)
        assertEquals(LocalTime.of(22, 0), OverlayPrefs.planPenceresi(app).first)
        assertEquals(LocalTime.of(7, 0), OverlayPrefs.planPenceresi(app).second)
    }
}
