package dev.ekrankarartma.s3

import android.app.Activity
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Tek ekran: durum, ana düğme, ton ve yoğunluk çipleri, gece planı ve gizlilik notu. Kaydırma yok. */
class MainActivity : Activity() {

    private lateinit var statusView: TextView
    private lateinit var hintView: TextView
    private lateinit var primaryButton: Button
    private lateinit var toneGroup: RadioGroup
    private lateinit var intensityGroup: RadioGroup
    private lateinit var planSwitch: Switch
    private lateinit var startButton: Button
    private lateinit var endButton: Button
    private lateinit var root: View

    /** Açık saat seçici; döndürme gibi durumlarda pencere sızmasın diye yok edilirken kapatılır. */
    private var zamanSecici: TimePickerDialog? = null

    /** render() sırasında programatik değişiklikler kullanıcı seçimi sayılmasın diye. */
    private var suppressEvents = false
    private val degisimDinleyici: () -> Unit = { render() }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusView = findViewById(R.id.status)
        hintView = findViewById(R.id.hint)
        primaryButton = findViewById(R.id.primary_button)
        toneGroup = findViewById(R.id.tone_group)
        intensityGroup = findViewById(R.id.intensity_group)
        planSwitch = findViewById(R.id.plan_switch)
        startButton = findViewById(R.id.plan_start)
        endButton = findViewById(R.id.plan_end)
        root = findViewById(R.id.root)
        applySystemBarInsets()

        primaryButton.setOnClickListener { onPrimaryClick() }
        toneGroup.setOnCheckedChangeListener { _, checkedId ->
            if (suppressEvents) return@setOnCheckedChangeListener
            val ton = toneIds.entries.firstOrNull { it.value == checkedId }?.key
                ?: return@setOnCheckedChangeListener
            OverlayPrefs.setTon(this, ton)
            OverlayController.refreshLook(this)
            render()
        }
        intensityGroup.setOnCheckedChangeListener { _, checkedId ->
            if (suppressEvents) return@setOnCheckedChangeListener
            val yogunluk = yogunlukIds.entries.firstOrNull { it.value == checkedId }?.key
                ?: return@setOnCheckedChangeListener
            OverlayPrefs.setYogunluk(this, yogunluk)
            OverlayController.refreshLook(this)
            render()
        }
        planSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressEvents) return@setOnCheckedChangeListener
            OverlayController.planSet(this, checked)
            if (checked && OverlayPrefs.planBekliyor(this)) openExactAlarmSettings()
            render()
        }
        startButton.setOnClickListener { pickTime(isStart = true) }
        endButton.setOnClickListener { pickTime(isStart = false) }

        if (savedInstanceState == null) requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        reconcileWithService()
        OverlayController.onAppResumed(this)
        OverlayService.dinleyiciEkle(degisimDinleyici)
        render()
    }

    override fun onPause() {
        OverlayService.dinleyiciCikar(degisimDinleyici)
        super.onPause()
    }

    override fun onDestroy() {
        zamanSecici?.dismiss()
        zamanSecici = null
        super.onDestroy()
    }

    /**
     * targetSdk 36 ile (Android 15+) pencere kenardan kenara çizilir. Başlık ve gizlilik satırının durum ve
     * gezinme çubuklarının altına girmemesi için sistem çubuğu boşluğu taban dolgunun üstüne eklenir.
     */
    private fun applySystemBarInsets() {
        val taban = Rect(root.paddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom)
        root.setOnApplyWindowInsetsListener { view, insets ->
            val cubuklar = sistemCubuguInsetleri(insets)
            view.setPadding(
                taban.left + cubuklar.left,
                taban.top + cubuklar.top,
                taban.right + cubuklar.right,
                taban.bottom + cubuklar.bottom
            )
            insets
        }
    }

    @Suppress("DEPRECATION") // API 30 öncesi için systemWindowInset*; 30+ dalı getInsets kullanır.
    private fun sistemCubuguInsetleri(insets: WindowInsets): Rect =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            Rect(bars.left, bars.top, bars.right, bars.bottom)
        } else {
            Rect(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
        }

    /** Tercihi gerçek servis durumuna ve izne göre düzeltir. */
    private fun reconcileWithService() {
        val canDraw = Settings.canDrawOverlays(this)
        val running = OverlayService.isRunning
        if (!canDraw && running) OverlayService.stop(this)
        OverlayPrefs.setOverlayOn(this, canDraw && running)
    }

    private fun onPrimaryClick() {
        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings()
            return
        }
        OverlayController.manualSet(this, !OverlayPrefs.overlayOn(this))
        render()
    }

    private fun pickTime(isStart: Boolean) {
        val (baslangic, bitis) = OverlayPrefs.planPenceresi(this)
        val secili = if (isStart) baslangic else bitis
        zamanSecici?.dismiss()
        zamanSecici = TimePickerDialog(
            this,
            { _, saat, dakika ->
                val yeni = LocalTime.of(saat, dakika)
                val yeniBaslangic = if (isStart) yeni else baslangic
                val yeniBitis = if (isStart) bitis else yeni
                if (!OverlayController.planWindowChanged(this, yeniBaslangic, yeniBitis)) {
                    Toast.makeText(this, R.string.toast_same_times, Toast.LENGTH_SHORT).show()
                }
                render()
            },
            secili.hour,
            secili.minute,
            true
        ).also { it.show() }
    }

    private fun render() {
        val canDraw = Settings.canDrawOverlays(this)
        val planAcik = OverlayPrefs.planAcik(this)
        val bekliyor = planAcik && OverlayPrefs.planBekliyor(this)
        val acik = canDraw && OverlayPrefs.overlayOn(this)
        val ton = OverlayPrefs.ton(this)
        val yogunluk = OverlayPrefs.yogunluk(this)
        val (baslangic, bitis) = OverlayPrefs.planPenceresi(this)

        statusView.text = when {
            !canDraw -> getString(R.string.status_permission)
            acik -> getString(
                R.string.status_on_format,
                getString(ton.etiketKimligi),
                getString(yogunluk.etiketKimligi)
            )
            bekliyor -> getString(R.string.status_alarm_permission)
            planAcik -> getString(R.string.status_plan_format, baslangic.hhmm(), bitis.hhmm())
            else -> getString(R.string.status_off)
        }
        primaryButton.setText(
            when {
                !canDraw -> R.string.button_permission
                acik -> R.string.button_turn_off
                else -> R.string.button_turn_on
            }
        )
        hintView.setText(
            when {
                !canDraw -> R.string.hint_permission
                bekliyor -> R.string.hint_alarm_permission
                else -> R.string.hint_tile
            }
        )

        suppressEvents = true
        try {
            toneGroup.check(toneIds.getValue(ton))
            intensityGroup.check(yogunlukIds.getValue(yogunluk))
            planSwitch.isChecked = planAcik
        } finally {
            suppressEvents = false
        }
        startButton.text = getString(R.string.plan_start_button, baslangic.hhmm())
        endButton.text = getString(R.string.plan_end_button, bitis.hhmm())
        startButton.isEnabled = planAcik
        endButton.isEnabled = planAcik
    }

    private fun openOverlaySettings() {
        // Android 11+ bu intent genel listeyi açar; kullanıcı listede uygulamayı kendisi bulmalı.
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_settings_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    /** Plan için tam zamanlı alarm izni. Android 12 (API 31) ve üzerinde gerekir. */
    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.toast_settings_unavailable, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Bildirim izni reddedilse de karartma çalışır; yalnızca bildirim gösterilmez. */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
        }
    }

    private fun LocalTime.hhmm(): String = format(HHMM)

    private companion object {
        const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
        const val REQUEST_NOTIFICATIONS = 1
        val HHMM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
