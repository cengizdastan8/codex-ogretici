package dev.ekrankarartma.s1

import android.content.Intent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

/** Hızlı ayar kutucuğunun köprüsü: dokunuşun aç/kapa kararı. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OverlayBridgeActivityTest {

    private val app = RuntimeEnvironment.getApplication()

    @Before
    fun resetState() {
        OverlayServiceHarness.reset()
        OverlayPrefs.setEnabled(app, false)
        ShadowSettings.setCanDrawOverlays(true)
    }

    @After
    fun tearDown() {
        OverlayServiceHarness.reset()
    }

    @Test
    fun idleBridgeStartsOverlayService() {
        Robolectric.buildActivity(OverlayBridgeActivity::class.java).setup()

        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun bridgeTurnsOffRunningOverlay() {
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)
        assertTrue(OverlayService.isRunning)

        Robolectric.buildActivity(OverlayBridgeActivity::class.java).setup()

        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun secondTapWhileStartIsStillPendingTurnsOff() {
        // İlk dokunuş başlatma isteği gönderdi; servis henüz yanıt vermedi.
        OverlayService.start(app)
        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)

        Robolectric.buildActivity(OverlayBridgeActivity::class.java).setup()

        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun bridgeOpensMainActivityWhenPermissionIsMissing() {
        ShadowSettings.setCanDrawOverlays(false)
        val bridge = Robolectric.buildActivity(OverlayBridgeActivity::class.java).setup().get()

        val next: Intent = shadowOf(bridge).nextStartedActivity
        assertEquals(MainActivity::class.java.name, next.component?.className)
    }
}
