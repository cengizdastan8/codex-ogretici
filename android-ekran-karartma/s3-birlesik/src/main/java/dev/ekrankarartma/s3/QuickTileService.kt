package dev.ekrankarartma.s3

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

/** Hızlı ayar kutucuğu: tıklanınca karartmayı açar/kapatır, izin yoksa uygulamayı açar. */
class QuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        // Karartma, açılıp hemen kapanan saydam köprü Activity'den değiştirilir (arka plan başlatma kısıtı).
        val bridge = Intent(this, OverlayBridgeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this, 0, bridge,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pending)
        } else {
            startActivityLegacy(bridge)
        }
    }

    /**
     * API 34 öncesi yol. Intent overload'u API 34'te kullanımdan kaldırıldı; belge yalnızca "API 34 ve üzeri için
     * PendingIntent overload'unu kullanın" diyor, istisna koşulunu açıkça yazmıyor. Çağrı başarısız olursa
     * UnsupportedOperationException yakalanır ve karartma doğrudan açılıp kapatılır. Bu yedek yol arka plan
     * başlatma kısıtına takılabilir; o zaman kullanıcı uygulamayı açarak durumu görür. Bu yol cihazda doğrulanmadı.
     */
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startActivityLegacy(bridge: Intent) {
        try {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(bridge)
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "Köprü Activity'si başlatılamadı; kutucuk doğrudan geçiş yapıyor.", e)
            val uygulama = applicationContext
            unlockAndRun {
                val hedefAcik = !OverlayService.isOnOrStarting
                OverlayController.manualSet(uygulama, hedefAcik)
                if (hedefAcik && !OverlayService.isOnOrStarting) {
                    // Açılamadı: sessiz kalmak yerine durumu gösteren uygulamayı aç.
                    uygulama.startActivity(
                        Intent(uygulama, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        tile.label = getString(R.string.tile_label)
        val active = OverlayService.isRunning && Settings.canDrawOverlays(this)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    private companion object {
        const val TAG = "QuickTileService"
    }
}
