package dev.ekrankarartma.s3

import android.content.Context
import android.graphics.Insets
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.AbsListView
import android.widget.Button
import android.widget.CompoundButton
import android.widget.HorizontalScrollView
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowSettings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-mdpi")
class MainActivityTest {

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
    fun tekBirincilDugmeVarVeKaydirmaKapsayicisiYok() {
        val activity = launch()
        val all = activity.findViewById<View>(R.id.root).descendantsIncludingSelf()

        val primary = activity.findViewById<Button>(R.id.primary_button)
        // Switch ve RadioButton (çipler) framework'te Button'ın alt sınıfıdır; düz düğmeler ayrıca sayılır.
        val tamGenislik = all.filterIsInstance<Button>()
            .filter { it !is CompoundButton }
            .filter { it.layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT }
        assertEquals(listOf(primary), tamGenislik)
        assertTrue(primary.minimumHeight >= dp(76))

        val scrollContainers = all.filter {
            it is ScrollView || it is HorizontalScrollView || it is AbsListView ||
                it.javaClass.simpleName.let { name ->
                    name.contains("RecyclerView") || name.contains("ViewPager")
                }
        }
        assertTrue("Kaydırılabilir kapsayıcı bulundu: $scrollContainers", scrollContainers.isEmpty())
    }

    @Test
    fun izinYokkenDugmeOverlayIzniIntentiniTetikler() {
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
    fun izinDonusundeArayuzKendiligindenIlerler() {
        ShadowSettings.setCanDrawOverlays(false)
        val controller = launchController()
        val activity = controller.get()
        val button = activity.findViewById<Button>(R.id.primary_button)
        assertEquals(activity.getString(R.string.button_permission), button.text.toString())

        ShadowSettings.setCanDrawOverlays(true)
        controller.pause().resume()

        assertEquals(activity.getString(R.string.button_turn_on), button.text.toString())
        assertEquals(
            activity.getString(R.string.status_off),
            activity.findViewById<TextView>(R.id.status).text.toString()
        )
    }

    @Test
    fun bayatAcikTercihGercekServisDurumunaGoreDuzeltilir() {
        // Süreç öldü: tercih "açık" ama servis çizmiyor.
        OverlayPrefs.setOverlayOn(app, true)

        val activity = launch()

        assertFalse(OverlayPrefs.overlayOn(app))
        assertEquals(
            activity.getString(R.string.status_off),
            activity.findViewById<TextView>(R.id.status).text.toString()
        )
    }

    @Test
    fun izinVarkenDugmeKarartmayiAcarVeKapatir() {
        val activity = launch()
        val button = activity.findViewById<Button>(R.id.primary_button)
        val status = activity.findViewById<TextView>(R.id.status)
        assertEquals(activity.getString(R.string.status_off), status.text.toString())
        assertEquals(activity.getString(R.string.button_turn_on), button.text.toString())

        button.performClick()
        assertEquals(activity.getString(R.string.button_turn_off), button.text.toString())
        assertEquals(
            activity.getString(
                R.string.status_on_format,
                activity.getString(R.string.ton_notr),
                activity.getString(R.string.yogunluk_orta)
            ),
            status.text.toString()
        )
        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)

        button.performClick()
        assertEquals(activity.getString(R.string.button_turn_on), button.text.toString())
        assertEquals(activity.getString(R.string.status_off), status.text.toString())
        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun tonSecimiAnindaGuncellenirVeServisYenidenBaslamaz() {
        OverlayService.isRunning = true
        OverlayPrefs.setOverlayOn(app, true)
        val activity = launch()

        activity.findViewById<RadioButton>(R.id.tone_warm).performClick()

        assertEquals(Ton.SICAK, OverlayPrefs.ton(app))
        assertEquals(OverlayService.ACTION_UPDATE, shadowOf(app).nextStartedService.action)
        assertNull(shadowOf(app).nextStartedService)
        assertTrue(activity.findViewById<RadioButton>(R.id.tone_warm).isChecked)
    }

    @Test
    fun yogunlukSecimiAnindaGuncellenirVeServisYenidenBaslamaz() {
        OverlayService.isRunning = true
        OverlayPrefs.setOverlayOn(app, true)
        val activity = launch()

        activity.findViewById<RadioButton>(R.id.intensity_strong).performClick()

        assertEquals(Yogunluk.GUCLU, OverlayPrefs.yogunluk(app))
        assertEquals(OverlayService.ACTION_UPDATE, shadowOf(app).nextStartedService.action)
        assertNull(shadowOf(app).nextStartedService)
        assertTrue(activity.findViewById<RadioButton>(R.id.intensity_strong).isChecked)
    }

    @Test
    fun planPenceresindeManuelKapatmaBitiseKadarKapaliKalir() {
        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 23, 0) }
        OverlayPrefs.setPlanPenceresi(app, LocalTime.of(22, 0), LocalTime.of(7, 0))
        OverlayPrefs.setPlanAcik(app, true)
        OverlayPrefs.setOverlayOn(app, true)
        OverlayService.isRunning = true
        val activity = launch()

        activity.findViewById<Button>(R.id.primary_button).performClick()

        val gecersiz = OverlayPrefs.gecersizKilma(app)
        assertNotNull(gecersiz)
        assertFalse(gecersiz!!.acik)
        assertEquals(LocalDateTime.of(2026, 11, 24, 7, 0), gecersiz.bitis)
        assertEquals(OverlayService.ACTION_STOP, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun planPenceresiDisindaManuelAcmaBitiseKadarAcikKalir() {
        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 10, 0) }
        OverlayPrefs.setPlanPenceresi(app, LocalTime.of(22, 0), LocalTime.of(7, 0))
        OverlayPrefs.setPlanAcik(app, true)
        val activity = launch()

        activity.findViewById<Button>(R.id.primary_button).performClick()

        val gecersiz = OverlayPrefs.gecersizKilma(app)
        assertNotNull(gecersiz)
        assertTrue(gecersiz!!.acik)
        assertEquals(LocalDateTime.of(2026, 11, 23, 22, 0), gecersiz.bitis)
        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)
    }

    @Test
    fun planAcilinca_pencereIcindeKarartmaHemenAcilirVeAlarmKurulur() {
        Saat.simdi = { LocalDateTime.of(2026, 11, 23, 23, 0) }
        val activity = launch()

        activity.findViewById<Switch>(R.id.plan_switch).performClick()

        assertEquals(OverlayService.ACTION_START, shadowOf(app).nextStartedService.action)
        val beklenen = LocalDateTime.of(2026, 11, 24, 7, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(beklenen, (app.getSystemService(android.app.AlarmManager::class.java)).nextAlarmClock.triggerTime)
    }

    @Test
    fun planAcilirkenAlarmIzniYoksaAlarmIzniEkraniAcilir() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val activity = launch()

        activity.findViewById<Switch>(R.id.plan_switch).performClick()

        assertTrue(OverlayPrefs.planBekliyor(app))
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, shadowOf(activity).nextStartedActivity.action)
        assertEquals(
            activity.getString(R.string.status_alarm_permission),
            activity.findViewById<TextView>(R.id.status).text.toString()
        )
    }

    @Test
    fun acikSaatSeciciDondurmedeKapanir() {
        OverlayPrefs.setPlanAcik(app, true)
        val controller = launchController()
        controller.get().findViewById<Button>(R.id.plan_start).performClick()
        val secici = ShadowDialog.getLatestDialog()
        assertNotNull(secici)
        assertTrue(secici.isShowing)

        controller.pause().stop().destroy()

        assertFalse(secici.isShowing)
    }

    @Test
    fun sistemCubuklariniKenarBoslugunaEkler() {
        val activity = launch()
        val root = activity.findViewById<View>(R.id.root)

        root.dispatchApplyWindowInsets(sistemCubuklari(ustDp = 32, altDp = 48))

        assertEquals(dp(16), root.paddingLeft)
        assertEquals(dp(12 + 32), root.paddingTop)
        assertEquals(dp(12 + 48), root.paddingBottom)
    }

    /**
     * 360x640 dp: gerçek sistem çubuğu insetleri uygulanmış halde (üstte 32dp durum çubuğu, altta 48dp
     * gezinme çubuğu) ton, yoğunluk, plan ve izin durumlarının tüm bileşimleri ekrana sığmalı.
     * Not: Robolectric metin satırlarını cihaz yazı tipiyle ölçmez; satır sarmalaması cihazda ayrıca doğrulanmalı.
     */
    @Test
    fun olcum360x640_butunDurumlardaKaydirmadanSigar() {
        val butce = dp(640)
        val hatalar = mutableListOf<String>()
        for (canDraw in listOf(false, true)) {
            for (plan in PlanDurum.values()) {
                for (acikMi in listOf(false, true)) {
                    if (acikMi && !canDraw) continue
                    prepare(canDraw, plan, acikMi)
                    val activity = launch()
                    val root = activity.findViewById<View>(R.id.root)
                    root.dispatchApplyWindowInsets(sistemCubuklari(ustDp = 32, altDp = 48))
                    for (ton in toneIds.keys) {
                        for (yogunluk in yogunlukIds.keys) {
                            activity.findViewById<RadioButton>(toneIds.getValue(ton)).performClick()
                            activity.findViewById<RadioButton>(yogunlukIds.getValue(yogunluk)).performClick()
                            root.measure(
                                View.MeasureSpec.makeMeasureSpec(dp(360), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.UNSPECIFIED
                            )
                            if (root.measuredHeight > butce) {
                                hatalar += "izin=$canDraw plan=$plan acik=$acikMi ton=$ton yogunluk=$yogunluk " +
                                    "yükseklik=${root.measuredHeight}px bütçe=${butce}px"
                            }
                        }
                    }
                }
            }
        }
        assertTrue("Sığmayan durumlar:\n" + hatalar.joinToString("\n"), hatalar.isEmpty())
    }

    private enum class PlanDurum { KAPALI, ACIK, BEKLIYOR }

    private val toneIds = mapOf(
        Ton.NOTR to R.id.tone_neutral,
        Ton.SICAK to R.id.tone_warm,
        Ton.MAT to R.id.tone_matte
    )
    private val yogunlukIds = mapOf(
        Yogunluk.HAFIF to R.id.intensity_light,
        Yogunluk.ORTA to R.id.intensity_medium,
        Yogunluk.GUCLU to R.id.intensity_strong
    )

    private fun prepare(canDraw: Boolean, plan: PlanDurum, acik: Boolean) {
        app.getSharedPreferences("karartma", Context.MODE_PRIVATE).edit().clear().commit()
        ShadowSettings.setCanDrawOverlays(canDraw)
        ShadowAlarmManager.setCanScheduleExactAlarms(plan != PlanDurum.BEKLIYOR)
        OverlayService.isRunning = acik
        OverlayPrefs.setOverlayOn(app, acik)
        OverlayPrefs.setPlanAcik(app, plan != PlanDurum.KAPALI)
        OverlayPrefs.setPlanPenceresi(app, LocalTime.of(22, 0), LocalTime.of(7, 0))
    }

    private fun launch(): MainActivity = launchController().get()

    private fun launchController(): ActivityController<MainActivity> =
        Robolectric.buildActivity(MainActivity::class.java).setup()

    private fun sistemCubuklari(ustDp: Int, altDp: Int): WindowInsets =
        WindowInsets.Builder()
            .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, dp(ustDp), 0, dp(altDp)))
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
