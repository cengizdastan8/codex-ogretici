package dev.ekrankarartma.s1

import android.app.Activity
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
import android.widget.TextView
import android.widget.Toast

/** Tek ekran: durum, ana düğme, izin/ipucu satırı ve gizlilik notu. */
class MainActivity : Activity() {

    private lateinit var root: View
    private lateinit var statusView: TextView
    private lateinit var hintView: TextView
    private lateinit var primaryButton: Button

    /** Servis durumu değişince (açıldı, kapandı, izin geri alındı) arayüz yeniden çizilir. */
    private val serviceStateListener: () -> Unit = { render() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root = findViewById(R.id.root)
        statusView = findViewById(R.id.status)
        hintView = findViewById(R.id.hint)
        primaryButton = findViewById(R.id.primary_button)
        primaryButton.setOnClickListener { onPrimaryClick() }
        applySystemBarInsets()

        if (savedInstanceState == null) requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        OverlayService.addStateListener(serviceStateListener)
        reconcileWithService()
        render()
    }

    override fun onPause() {
        OverlayService.removeStateListener(serviceStateListener)
        super.onPause()
    }

    private fun onPrimaryClick() {
        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings()
            return
        }
        // Yalnızca istek gönderilir. Sonuç (açıldı / izin yok / hata) servisten serviceStateListener ile gelir.
        if (OverlayService.isOnOrStarting) OverlayService.stop(this) else OverlayService.start(this)
    }

    /** Tercihi gerçek servis durumuna ve izne göre düzeltir. */
    private fun reconcileWithService() {
        val canDraw = Settings.canDrawOverlays(this)
        if (!canDraw && OverlayService.isRunning) OverlayService.stop(this)
        OverlayPrefs.setEnabled(this, canDraw && OverlayService.isRunning)
    }

    private fun render() {
        val canDraw = Settings.canDrawOverlays(this)
        val on = canDraw && OverlayService.isRunning
        val (statusRes, buttonRes, hintRes) = when {
            !canDraw -> Triple(R.string.status_permission, R.string.button_permission, R.string.hint_permission)
            on -> Triple(R.string.status_on, R.string.button_turn_off, R.string.hint_tile)
            else -> Triple(R.string.status_off, R.string.button_turn_on, R.string.hint_tile)
        }
        statusView.setText(statusRes)
        primaryButton.setText(buttonRes)
        hintView.setText(hintRes)
    }

    /**
     * targetSdk 36 ile pencere kenardan kenara çizilir ve bu sürümde bundan çıkılamaz. Başlık ve gizlilik
     * satırının durum ve gezinme çubuklarının altına girmemesi için sistem çubuğu boşluğu, taban 16dp
     * dolgunun üstüne eklenir.
     */
    private fun applySystemBarInsets() {
        val base = Rect(root.paddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom)
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = systemBarInsets(insets)
            view.setPadding(
                base.left + bars.left,
                base.top + bars.top,
                base.right + bars.right,
                base.bottom + bars.bottom
            )
            insets
        }
    }

    @Suppress("DEPRECATION") // API 30 öncesi için systemWindowInset*; 30+ dalı getInsets kullanır.
    private fun systemBarInsets(insets: WindowInsets): Rect =
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

    private fun openOverlaySettings() {
        // Android 11+ bu intent genel listeyi açar; kullanıcı listede uygulamayı kendisi bulmalı.
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_settings_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    /** Bildirim izni reddedilse de karartma çalışır; yalnızca bildirim gösterilmez. */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
        }
    }

    private companion object {
        const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
        const val REQUEST_NOTIFICATIONS = 1
    }
}
