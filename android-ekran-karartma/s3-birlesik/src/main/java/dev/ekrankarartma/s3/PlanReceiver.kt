package dev.ekrankarartma.s3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Plan sınırı alarmı. Yalnızca uygulamanın kendi kurduğu alarm bu alıcıyı tetikler (dışa kapalı). */
class PlanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PlanAlarm.ACTION_SINIR) {
            OverlayController.onPlanBoundary(context)
        }
    }
}
