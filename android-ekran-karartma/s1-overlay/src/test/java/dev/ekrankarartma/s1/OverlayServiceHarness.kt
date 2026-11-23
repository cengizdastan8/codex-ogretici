package dev.ekrankarartma.s1

import android.content.Intent
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ServiceController

/**
 * Testte tek bir servis örneği tutar. Gerçek Android'de START ve STOP aynı çalışan servise gider;
 * her komutta yeni örnek açmak durumu bozar (STOP, START'ın eklediği katmanı bulamaz).
 */
internal object OverlayServiceHarness {

    private var controller: ServiceController<OverlayService>? = null

    /** Komutu servise iletir. Servis yoksa oluşturur. */
    fun deliver(action: String) {
        val app = RuntimeEnvironment.getApplication()
        val current = controller ?: Robolectric.buildService(OverlayService::class.java).create().also {
            controller = it
        }
        current.get().onStartCommand(Intent(app, OverlayService::class.java).setAction(action), 0, 1)
    }

    /** Süreç öldürülmüş gibi davranır: katmanı kaldırır ve durum bayraklarını sıfırlar. */
    fun reset() {
        controller?.destroy()
        controller = null
        OverlayService.isRunning = false
        OverlayService.isStartPending = false
    }
}
