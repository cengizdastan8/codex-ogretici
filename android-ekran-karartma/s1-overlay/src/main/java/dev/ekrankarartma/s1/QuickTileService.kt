package dev.ekrankarartma.s1

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** Hızlı ayar kutucuğu: tıklanınca karartmayı açar/kapatır, izin yoksa uygulamayı açar. */
class QuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    // Intent overload'u API 34'te kullanımdan kaldırıldı; minSdk 26 için 34 öncesinde tek seçenek budur.
    // Lint dal korumasını göremediği için hatayı bu noktada bastırıyoruz.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        // Arka plandan FGS başlatma kısıtları için servis, açılıp hemen kapanan saydam köprü Activity'den başlatılır.
        val bridge = Intent(this, OverlayBridgeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            val pending = PendingIntent.getActivity(
                this, 0, bridge,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(bridge)
        }
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        tile.label = getString(R.string.tile_label)
        val active = OverlayService.isRunning && Settings.canDrawOverlays(this)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
