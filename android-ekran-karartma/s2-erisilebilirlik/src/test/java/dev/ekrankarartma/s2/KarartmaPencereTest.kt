package dev.ekrankarartma.s2

import android.os.Build
import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KarartmaPencereTest {

    private val parametreler = karartmaLayoutParams()

    @Test
    fun tipAccessibilityOverlayVeAlfa095() {
        assertEquals(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, parametreler.type)
        assertEquals(0.95f, KARARTMA_ALFASI, 0f)
        assertEquals(0.95f, parametreler.alpha, 0.0001f)
    }

    @Test
    fun dokunmaVeOdakBayraklariVar() {
        val bayraklar = parametreler.flags
        assertTrue(bayraklar and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE != 0)
        assertTrue(bayraklar and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0)
        assertTrue(bayraklar and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN != 0)
        assertTrue(bayraklar and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS != 0)
    }

    @Test
    fun kesikAlanModuTumEkranaAyarli() {
        if (Build.VERSION.SDK_INT >= 30) {
            assertEquals(
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS,
                parametreler.layoutInDisplayCutoutMode
            )
        }
    }
}
