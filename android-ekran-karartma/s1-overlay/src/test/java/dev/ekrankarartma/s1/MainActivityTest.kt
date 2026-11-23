package dev.ekrankarartma.s1

import android.graphics.Insets
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.AbsListView
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-mdpi")
class MainActivityTest {

    private val app = RuntimeEnvironment.getApplication()
    private val controllers = mutableListOf<ActivityController<MainActivity>>()

    @Before
    fun resetState() {
        OverlayServiceHarness.reset()
        OverlayPrefs.setEnabled(app, false)
    }

    @After
    fun tearDown() {
        // onPause servis dinleyicisini çıkarır; eski Activity sonraki testlerde çizim yapmasın.
        controllers.forEach { it.pause().stop().destroy() }
        controllers.clear()
        OverlayServiceHarness.reset()
    }

    @Test
    fun hasOneFullWidthPrimaryButtonAndNoScrollContainers() {
        val activity = launch()
        val all = activity.findViewById<View>(R.id.root).descendantsIncludingSelf()

        val buttons = all.filterIsInstance<Button>()
        assertEquals(1, buttons.size)
        val primary = activity.findViewById<Button>(R.id.primary_button)
        assertTrue(buttons.single() === primary)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, primary.layoutParams.width)
        assertTrue(primary.minimumHeight >= dp(120))

        val scrollContainers = all.filter {
            it is ScrollView || it is HorizontalScrollView || it is AbsListView ||
                it.javaClass.simpleName.let { name ->
                    name.contains("RecyclerView") || name.contains("ViewPager")
                }
        }
        assertTrue("Kaydırılabilir kapsayıcı bulundu: $scrollContainers", scrollContainers.isEmpty())
    }

    @Test
    fun withoutPermissionPrimaryButtonOpensOverlayPermissionForThisPackage() {
        ShadowSettings.setCanDrawOverlays(false)
        val activity = launch()
        val button = activity.findViewById<Button>(R.id.primary_button)
        assertEquals(activity.getString(R.string.button_permission), button.text.toString())

        button.performClick()

        val started = shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, started.action)
        assertEquals("package", started.data?.scheme)
        assertEquals(app.packageName, started.data?.schemeSpecificPart)
    }

    @Test
    fun withPermissionPrimaryButtonTogglesDimmingState() {
        ShadowSettings.setCanDrawOverlays(true)
        val activity = launch()
        val button = activity.findViewById<Button>(R.id.primary_button)
        val status = activity.findViewById<TextView>(R.id.status)
        assertEquals(activity.getString(R.string.button_turn_on), button.text.toString())
        assertEquals(activity.getString(R.string.status_off), status.text.toString())

        button.performClick()
        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)
        assertTrue(OverlayService.isRunning)
        assertEquals(activity.getString(R.string.button_turn_off), button.text.toString())
        assertEquals(activity.getString(R.string.status_on), status.text.toString())

        button.performClick()
        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
        OverlayServiceHarness.deliver(OverlayService.ACTION_STOP)
        assertFalse(OverlayService.isRunning)
        assertEquals(activity.getString(R.string.button_turn_on), button.text.toString())
        assertEquals(activity.getString(R.string.status_off), status.text.toString())
    }

    @Test
    fun uiReflectsServiceOutcomeNotTheClickedIntent() {
        ShadowSettings.setCanDrawOverlays(true)
        val activity = launch()
        val status = activity.findViewById<TextView>(R.id.status)

        activity.findViewById<Button>(R.id.primary_button).performClick()
        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)
        // Servis çalışmadan izin geri alınır: başlatma reddedilmeli ve arayüz "Açık" göstermemeli.
        ShadowSettings.setCanDrawOverlays(false)
        OverlayServiceHarness.deliver(OverlayService.ACTION_START)

        assertFalse(OverlayService.isRunning)
        assertFalse(OverlayPrefs.isEnabled(app))
        assertEquals(activity.getString(R.string.status_permission), status.text.toString())
    }

    @Test
    fun contentFitsIn360x640DpWithoutScrollingAfterSystemBarInsets() {
        for (permissionGranted in listOf(false, true)) {
            ShadowSettings.setCanDrawOverlays(permissionGranted)
            val root = launch().findViewById<View>(R.id.root)
            // Durum çubuğu 24dp, gezinme çubuğu 48dp: insetler dahil toplam yükseklik ekrana sığmalı.
            root.dispatchApplyWindowInsets(systemBarInsets(top = 24, bottom = 48))
            root.measure(
                View.MeasureSpec.makeMeasureSpec(dp(360), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            )
            assertTrue(
                "izin=$permissionGranted: yükseklik ${root.measuredHeight}px, ekran ${dp(640)}px",
                root.measuredHeight <= dp(640)
            )
        }
    }

    @Test
    fun systemBarInsetsAreAddedToGutterSoContentDoesNotOverlapBars() {
        val root = launch().findViewById<View>(R.id.root)

        root.dispatchApplyWindowInsets(systemBarInsets(top = 24, bottom = 48))

        assertEquals(dp(16), root.paddingLeft)
        assertEquals(dp(16), root.paddingRight)
        assertEquals(dp(16) + dp(24), root.paddingTop)
        assertEquals(dp(16) + dp(48), root.paddingBottom)
    }

    @Test
    fun staleEnabledPreferenceIsCorrectedToOffOnResume() {
        ShadowSettings.setCanDrawOverlays(true)
        OverlayPrefs.setEnabled(app, true) // Servis çalışmıyor; tercih yanlış.

        val activity = launch()

        assertFalse(OverlayPrefs.isEnabled(app))
        assertEquals(
            activity.getString(R.string.status_off),
            activity.findViewById<TextView>(R.id.status).text.toString()
        )
    }

    @Test
    fun grantingOverlayPermissionAdvancesUiOnResume() {
        ShadowSettings.setCanDrawOverlays(false)
        val controller = launchController()
        val activity = controller.get()
        assertEquals(
            activity.getString(R.string.status_permission),
            activity.findViewById<TextView>(R.id.status).text.toString()
        )

        ShadowSettings.setCanDrawOverlays(true)
        controller.pause().resume()

        assertEquals(
            activity.getString(R.string.status_off),
            activity.findViewById<TextView>(R.id.status).text.toString()
        )
        assertEquals(
            activity.getString(R.string.button_turn_on),
            activity.findViewById<Button>(R.id.primary_button).text.toString()
        )
    }

    private fun launch(): MainActivity = launchController().get()

    private fun launchController(): ActivityController<MainActivity> {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        controllers += controller
        return controller
    }

    private fun systemBarInsets(top: Int, bottom: Int): WindowInsets =
        WindowInsets.Builder()
            .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, dp(top), 0, dp(bottom)))
            .build()

    private fun dp(value: Int): Int = (value * app.resources.displayMetrics.density).toInt()

    private fun View.descendantsIncludingSelf(): List<View> {
        val result = mutableListOf(this)
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                result += getChildAt(i).descendantsIncludingSelf()
            }
        }
        return result
    }
}
