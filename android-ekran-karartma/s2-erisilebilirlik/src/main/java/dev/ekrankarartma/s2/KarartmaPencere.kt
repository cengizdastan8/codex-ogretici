package dev.ekrankarartma.s2

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager

private const val ETIKET = "KarartmaPencere"

/**
 * Karartma seviyesi pencere alfasiyla (LayoutParams.alpha) verilir; view arka plani opak siyah kalir.
 *
 * DENEYSEL DEGER. Bu senaryo, 0.8 dokunma-gecirgenligi tavaninin (Android 12+) erisilebilirlik
 * katmaninda uygulanmadigi varsayimiyla test eder. Bu yuzden S1/S3'teki MAX_ALPHA = 0.75 kurali
 * bu modulde uygulanmaz ve alfa kirpilmaz. Cihaz testi kriteri: karartma aciksken dokunmalar
 * alttaki uygulamaya geciyor mu? Gecmiyorsa bu deger geri cekilmeli.
 */
internal const val KARARTMA_ALFASI = 0.95f

/**
 * Saf fabrika: yan etkisi yoktur, bu yuzden birim testinde dogrudan dogrulanabilir.
 * NOT_TOUCHABLE: dokunmalar bu katmanda durmaz, alttaki uygulamaya gecer.
 * NOT_FOCUSABLE: klavye odagi calmaz.
 */
internal fun karartmaLayoutParams(): WindowManager.LayoutParams {
    val parametreler = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    )
    parametreler.alpha = KARARTMA_ALFASI
    parametreler.setTitle("EkranKarartmaS2")
    // LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS sabiti API 30'da eklendi (lint InlinedApi).
    // Eski surumlerde degeri 28-29 icin gecersiz olabilir; bu yuzden yalnizca API 30+ icin ayarlanir.
    if (Build.VERSION.SDK_INT >= 30) {
        parametreler.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    }
    return parametreler
}

/** Tam ekran opak siyah katmani pencere yoneticisine ekler ve kaldirir. */
internal class KarartmaKatmani(
    private val pencereYoneticisi: WindowManager,
    private val baglam: Context
) {
    private var gorunum: View? = null

    val gorunuyorMu: Boolean
        get() = gorunum != null

    /** Katmani ekler. Eklenemezse (ornegin BadTokenException) false doner; katman olusmaz. */
    fun goster(): Boolean {
        if (gorunum != null) return true
        val katman = View(baglam).apply { setBackgroundColor(Color.BLACK) }
        return try {
            pencereYoneticisi.addView(katman, karartmaLayoutParams())
            gorunum = katman
            true
        } catch (e: WindowManager.BadTokenException) {
            Log.w(ETIKET, "Karartma katmani eklenemedi (BadToken)", e)
            false
        } catch (e: RuntimeException) {
            Log.w(ETIKET, "Karartma katmani eklenemedi", e)
            false
        }
    }

    /** Katman varsa kaldirir. Servis yok edilirken mutlaka cagrilir. */
    fun kaldir() {
        val katman = gorunum ?: return
        gorunum = null
        try {
            pencereYoneticisi.removeViewImmediate(katman)
        } catch (e: RuntimeException) {
            Log.w(ETIKET, "Karartma katmani kaldirilamadi", e)
        }
    }
}
