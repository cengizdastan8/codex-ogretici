package dev.ekrankarartma.s2

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/** Gorunum agacini duz listeye acar (kok dahil). */
internal fun tumGorunumler(gorunum: View): List<View> {
    val liste = mutableListOf(gorunum)
    if (gorunum is ViewGroup) {
        for (i in 0 until gorunum.childCount) {
            liste += tumGorunumler(gorunum.getChildAt(i))
        }
    }
    return liste
}

/** Erisilebilirlik servisini "etkin" olarak Settings.Secure'a yazar. */
internal fun erisilebilirlikServisiniEtkinlestir(baglam: Context) {
    val kimlik = ComponentName(baglam, KarartmaErisilebilirlikServisi::class.java).flattenToString()
    Settings.Secure.putString(
        baglam.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        kimlik
    )
}

internal fun erisilebilirlikServisiniDevreDisiBirak(baglam: Context) {
    Settings.Secure.putString(
        baglam.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        null
    )
}

/** Bildirim iznini Robolectric uzerinde verir ya da geri alir (API 33+ calisma zamani izni). */
internal fun bildirimIzniVer(verildi: Boolean) {
    val uygulama: Application = RuntimeEnvironment.getApplication()
    val golge = shadowOf(uygulama)
    if (verildi) {
        golge.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        golge.denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }
}
