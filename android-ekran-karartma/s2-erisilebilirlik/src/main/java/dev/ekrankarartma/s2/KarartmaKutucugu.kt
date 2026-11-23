package dev.ekrankarartma.s2

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Hizli ayar kutucugu. Servis bagliysa karartmayi dogrudan acar/kapatir.
 * Servis bagli degilse uygulamayi acar. Foreground service baslatmaz.
 */
class KarartmaKutucugu : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        durumuYansit()
    }

    override fun onClick() {
        super.onClick()
        val servis = KarartmaErisilebilirlikServisi.ornek
        if (servis != null) {
            servis.karartmayiAyarla(!KarartmaDurumu.tercihAcik(this))
            durumuYansit()
        } else {
            uygulamayiAc()
        }
    }

    private fun durumuYansit() {
        val kutucuk = qsTile ?: return
        kutucuk.state = if (KarartmaDurumu.aktifMi(this)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        kutucuk.updateTile()
    }

    private fun uygulamayiAc() {
        val niyet = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            val bekleyen = PendingIntent.getActivity(
                this,
                0,
                niyet,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(bekleyen)
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(niyet)
        }
    }
}
