package dev.ekrankarartma.s3

import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager

/**
 * Karartma yoğunluğunun üst sınırı. Android 12+ dokunma geçirgenliği tavanı yaklaşık 0.8;
 * altında kalmak için güvenli pay bırakılmıştır.
 */
const val MAX_ALPHA = 0.75f

/** İstenen alfayı [0, MAX_ALPHA] aralığına sıkıştırır. */
fun clampDimAlpha(requestedAlpha: Float): Float = requestedAlpha.coerceIn(0f, MAX_ALPHA)

/**
 * Tam ekran karartma penceresinin parametreleri. Saf fonksiyon: yan etkisi yok, context gerektirmez.
 * Karartma seviyesi pencere alfasıyla verilir; görünüm arka planı opak renktir.
 */
fun createOverlayLayoutParams(requestedAlpha: Float = MAX_ALPHA): WindowManager.LayoutParams {
    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    )
    params.alpha = clampDimAlpha(requestedAlpha)
    // LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS sabiti API 30'da eklendi; daha eski sürümlerde atlanır.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    }
    return params
}
