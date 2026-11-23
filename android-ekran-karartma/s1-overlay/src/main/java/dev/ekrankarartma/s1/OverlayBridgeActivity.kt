package dev.ekrankarartma.s1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

/**
 * Saydam köprü: hızlı ayar kutucuğu bu Activity'yi açar. Burada servis başlatılır/durdurulur
 * (ön plan Activity'den başlatma kısıtı olmaz) ve Activity hemen kapanır.
 * Karar, çalışan ya da açılmakta olan karartmaya göre verilir: açılış sürerken ikinci dokunuş kapatır.
 */
class OverlayBridgeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            !Settings.canDrawOverlays(this) -> startActivity(Intent(this, MainActivity::class.java))
            OverlayService.isOnOrStarting -> OverlayService.stop(this)
            else -> OverlayService.start(this)
        }
        finish()
    }
}
