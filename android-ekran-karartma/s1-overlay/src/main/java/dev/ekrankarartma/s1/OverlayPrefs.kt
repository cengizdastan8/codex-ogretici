package dev.ekrankarartma.s1

import android.content.Context

/** Karartmanın son bilinen tercihi ("karartma" SharedPreferences dosyası). Gerçek durum OverlayService.isRunning'dir. */
object OverlayPrefs {
    private const val FILE_NAME = "karartma"
    private const val KEY_ENABLED = "acik"

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
}
