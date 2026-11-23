package dev.ekrankarartma.s1

import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OverlayLayoutTest {

    @Test
    fun layoutParamsUseOverlayTypeRequiredFlagsAndCappedAlpha() {
        val params = createOverlayLayoutParams()

        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
        val requiredFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        assertEquals(requiredFlags, params.flags and requiredFlags)
        assertEquals(
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS,
            params.layoutInDisplayCutoutMode
        )
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, params.width)
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, params.height)
        assertTrue(params.alpha <= 0.75f)
        assertEquals(0.75f, params.alpha, 0f)
    }

    @Test
    fun requestedAlphaAboveMaxIsClampedTo075() {
        assertEquals(0.75f, createOverlayLayoutParams(0.9f).alpha, 0f)
        assertEquals(0.75f, createOverlayLayoutParams(1.0f).alpha, 0f)
        assertEquals(0.75f, clampDimAlpha(5f), 0f)
    }

    @Test
    fun lowerAlphaIsKeptAndNegativeAlphaIsClampedToZero() {
        assertEquals(0.3f, createOverlayLayoutParams(0.3f).alpha, 1e-6f)
        assertEquals(0f, clampDimAlpha(-1f), 0f)
    }

    @Test
    fun maxAlphaConstantIsZeroPointSeventyFive() {
        assertEquals(0.75f, MAX_ALPHA, 0f)
    }

    @Test
    fun cutoutAlwaysModeIsOnlyRequestedFromApi30() {
        val defaultMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        val alwaysMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS

        assertEquals(defaultMode, createOverlayLayoutParams(sdkInt = 28).layoutInDisplayCutoutMode)
        assertEquals(defaultMode, createOverlayLayoutParams(sdkInt = 29).layoutInDisplayCutoutMode)
        assertEquals(alwaysMode, createOverlayLayoutParams(sdkInt = 30).layoutInDisplayCutoutMode)
    }
}
