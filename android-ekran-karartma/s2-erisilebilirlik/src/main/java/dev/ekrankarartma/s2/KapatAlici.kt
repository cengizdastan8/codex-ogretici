package dev.ekrankarartma.s2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Bildirimdeki "Kapat" eylemini karsilar. Yalnizca uygulama ici (exported=false). */
class KapatAlici : BroadcastReceiver() {

    override fun onReceive(baglam: Context, niyet: Intent?) {
        if (niyet?.action != EYLEM) return
        val servis = KarartmaErisilebilirlikServisi.ornek
        if (servis != null) {
            servis.karartmayiAyarla(false)
        } else {
            KarartmaDurumu.tercihYaz(baglam, false)
            KarartmaBildirimi.iptal(baglam)
        }
    }

    companion object {
        const val EYLEM = "dev.ekrankarartma.s2.KARARTMAYI_KAPAT"
    }
}
