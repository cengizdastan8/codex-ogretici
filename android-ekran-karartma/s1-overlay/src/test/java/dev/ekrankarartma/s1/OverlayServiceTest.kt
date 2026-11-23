package dev.ekrankarartma.s1

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

/** Servisin gerçek başlatma/durdurma akışı: katman eklenir, kaldırılır ve durum yayınlanır. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OverlayServiceTest {

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
    fun startWithPermissionAddsOverlayAndNotifiesListeners() {
        var notifications = 0
        val listener: () -> Unit = { notifications++ }
        OverlayService.addStateListener(listener)
        try {
            OverlayServiceHarness.deliver(OverlayService.ACTION_START)

            assertTrue(OverlayService.isRunning)
            assertFalse(OverlayService.isStartPending)
            assertTrue(OverlayPrefs.isEnabled(app))
            assertTrue("Durum dinleyicisi çağrılmalı", notifications >= 1)
        } finally {
            OverlayService.removeStateListener(listener)
        }
    }

    @Test
    fun startAttachesOneFullScreenBlackLayerWithCappedOverlayType() {
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)

        // Bayrak tek başına yetmez: pencere yöneticisinde gerçekten bir katman olmalı.
        val roots = windowRoots()
        assertEquals("Pencere yöneticisindeki katman sayısı", 1, roots.size)
        val layer = roots.single()
        assertEquals(Color.BLACK, (layer.background as ColorDrawable).color)
        val params = layer.layoutParams as WindowManager.LayoutParams
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
        assertEquals(MAX_ALPHA, params.alpha, 0f)
    }

    @Test
    fun startWithoutPermissionDoesNotMarkRunning() {
        ShadowSettings.setCanDrawOverlays(false)

        OverlayServiceHarness.deliver(OverlayService.ACTION_START)

        assertFalse(OverlayService.isRunning)
        assertFalse(OverlayPrefs.isEnabled(app))
    }

    @Test
    fun stopRemovesOverlayAndClearsPreference() {
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)
        assertTrue(OverlayService.isRunning)

        OverlayServiceHarness.deliver(OverlayService.ACTION_STOP)

        assertFalse(OverlayService.isRunning)
        assertFalse(OverlayPrefs.isEnabled(app))
        assertTrue("Katman pencere yöneticisinden kaldırılmalı", windowRoots().isEmpty())
    }

    @Test
    fun destroyingServiceRemovesOverlay() {
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)
        assertTrue(OverlayService.isRunning)

        OverlayServiceHarness.reset() // onDestroy yolu: katman kaldırılmalı

        assertFalse(OverlayService.isRunning)
        assertFalse(OverlayPrefs.isEnabled(app))
        assertTrue("Servis yok edilirken katman kalmamalı", windowRoots().isEmpty())
    }

    /** Pencere yöneticisine eklenmiş kök görünümler (karartma katmanları). Hidden API: yalnızca test için. */
    private fun windowRoots(): List<View> {
        val global = Class.forName("android.view.WindowManagerGlobal").getMethod("getInstance").invoke(null)
        val field = global.javaClass.getDeclaredField("mViews").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        return (field.get(global) as List<View>).toList()
    }

    @Test
    fun isOnOrStartingCoversPendingStartAndRunningOverlay() {
        assertFalse(OverlayService.isOnOrStarting)
        OverlayService.isStartPending = true
        assertTrue(OverlayService.isOnOrStarting)
        OverlayService.isStartPending = false
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)
        assertEquals(true, OverlayService.isOnOrStarting)
    }
}
