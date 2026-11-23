package dev.ekrankarartma.s3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Bildirimdeki "Kapat" eylemi. Manuel kapatma sayılır; plan açıksa kural uygulanır. */
class KapatAlici : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_KAPAT) {
            OverlayController.manualSet(context, false)
        }
    }

    companion object {
        const val ACTION_KAPAT = "dev.ekrankarartma.s3.action.KAPAT"
    }
}
