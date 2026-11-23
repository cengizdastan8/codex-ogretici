package dev.ekrankarartma.s3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

/**
 * Saydam köprü: hızlı ayar kutucuğu bu Activity'yi açar. Burada karartma açılır/kapanır
 * (ön plan Activity'den başlatma kısıtı olmaz) ve Activity hemen kapanır.
 */
class OverlayBridgeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Açılış sürerken (servis henüz çizmemişken) gelen ikinci dokunuş da kapatır.
            OverlayController.manualSet(this, !OverlayService.isOnOrStarting)
        }
        finish()
    }
}
