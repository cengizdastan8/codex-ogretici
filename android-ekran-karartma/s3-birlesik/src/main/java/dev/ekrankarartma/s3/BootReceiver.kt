package dev.ekrankarartma.s3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Açılış ve saat/saat dilimi değişimi: yalnızca planın alarmını yeniden kurar.
 * Karartmayı kendiliğinden açmaz.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            OverlayController.syncPlan(context)
        }
    }
}
