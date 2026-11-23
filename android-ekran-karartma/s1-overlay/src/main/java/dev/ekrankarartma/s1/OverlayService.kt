package dev.ekrankarartma.s1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log
import android.view.View
import android.view.WindowManager
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Tüm ekranı karartan ön plan servisi. Katman TYPE_APPLICATION_OVERLAY ile çizilir.
 * Kapatma yolları: ana ekran düğmesi, bildirimdeki "Kapat", hızlı ayar kutucuğu.
 *
 * Durum her zaman burada gerçekleşir; tercih ve arayüz bu sonuca göre güncellenir ([publishState]).
 */
class OverlayService : Service() {

    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startOverlay()
            ACTION_STOP -> shutdown()
            else -> stopSelf()
        }
        // Sistem öldürürse karartma geri gelmesin; arayüz tercihi gerçek duruma göre düzeltir.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeOverlayView()
        publishState()
        super.onDestroy()
    }

    private fun startOverlay() {
        // startForegroundService() ile başlatıldıysa startForeground() 5 saniye içinde çağrılmalı.
        // İzin kontrolünden önce çağrılır ki izin yoksa da bu süre aşılmasın.
        startInForeground()
        isStartPending = false

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Üzerinde gösterme izni yok; servis kapanıyor.")
            shutdown()
            return
        }
        if (overlayView != null) return

        val view = View(this).apply { setBackgroundColor(Color.BLACK) }
        try {
            windowManager().addView(view, createOverlayLayoutParams())
        } catch (e: RuntimeException) {
            // WindowManager.BadTokenException, SecurityException vb.
            Log.e(TAG, "Karartma katmanı eklenemedi.", e)
            shutdown()
            return
        }
        overlayView = view
        isRunning = true
        publishState()
    }

    private fun shutdown() {
        isStartPending = false
        removeOverlayView()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        publishState()
    }

    private fun removeOverlayView() {
        val view = overlayView ?: return
        overlayView = null
        isRunning = false
        try {
            windowManager().removeViewImmediate(view)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Karartma katmanı kaldırılamadı.", e)
        }
    }

    /** Gerçek durumu tercihe yazar, kutucuğu günceller ve açık arayüzleri bilgilendirir. */
    private fun publishState() {
        OverlayPrefs.setEnabled(this, isRunning)
        requestTileRefresh()
        listeners.forEach { it() }
    }

    private fun windowManager(): WindowManager = getSystemService(WindowManager::class.java)

    @Suppress("DEPRECATION") // FOREGROUND_SERVICE_TYPE_NONE API 36'da işaretli; 29-33 için 2 parametreli çağrıyla aynı.
    private fun startInForeground() {
        // Kanal, bildirim oluşturulmadan önce var olmalı.
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val notification = buildNotification()
        when {
            // Android 14+: specialUse tipi manifestte bildirilmiş olmalı.
            Build.VERSION.SDK_INT >= 34 -> startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
            // Android 10-13: specialUse tipi yok; FOREGROUND_SERVICE_TYPE_NONE, 2 parametreli çağrıyla aynıdır.
            Build.VERSION.SDK_INT >= 29 -> startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
            else -> startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val closeAction = PendingIntent.getService(
            this, 1, intent(this, ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                    getString(R.string.notif_action_close),
                    closeAction
                ).build()
            )
            .build()
    }

    private fun requestTileRefresh() {
        TileService.requestListeningState(this, ComponentName(this, QuickTileService::class.java))
    }

    companion object {
        private const val TAG = "OverlayService"
        const val ACTION_START = "dev.ekrankarartma.s1.action.START"
        const val ACTION_STOP = "dev.ekrankarartma.s1.action.STOP"
        private const val CHANNEL_ID = "karartma"
        private const val NOTIFICATION_ID = 1

        private val listeners = CopyOnWriteArrayList<() -> Unit>()

        /** Karartma katmanı gerçekten çizili mi. Süreç öldüğünde bu bayrak da sıfırlanır. */
        @Volatile
        var isRunning: Boolean = false
            internal set

        /** Başlatma isteği gönderildi, servis henüz yanıt vermedi. Süreç öldüğünde sıfırlanır. */
        @Volatile
        var isStartPending: Boolean = false
            internal set

        /** Açık ya da açılıyor. Düğme ve kutucuk ikinci dokunuşta kapatma kararı vermek için bunu kullanır. */
        val isOnOrStarting: Boolean
            get() = isRunning || isStartPending

        /** Durum değiştiğinde (ana iş parçacığında) çağrılır. Activity onResume'da ekler, onPause'da çıkarır. */
        fun addStateListener(listener: () -> Unit) {
            listeners.addIfAbsent(listener)
        }

        fun removeStateListener(listener: () -> Unit) {
            listeners.remove(listener)
        }

        /**
         * Servisi ön plan olarak başlatır. Sonuç burada değil, servisin [publishState] çağrısıyla
         * gelir; arayüz bu yüzden yalnızca [addStateListener] üzerinden güncellenir.
         */
        fun start(context: Context) {
            isStartPending = true
            try {
                context.startForegroundService(intent(context, ACTION_START))
            } catch (e: RuntimeException) {
                Log.e(TAG, "Servis başlatılamadı.", e)
                isStartPending = false
                OverlayPrefs.setEnabled(context, false)
            }
        }

        /** Çalışan servise kapatma isteği gönderir. */
        fun stop(context: Context) {
            isStartPending = false
            try {
                context.startService(intent(context, ACTION_STOP))
            } catch (e: RuntimeException) {
                Log.w(TAG, "Servise durdurma isteği gönderilemedi.", e)
            }
        }

        private fun intent(context: Context, action: String): Intent =
            Intent(context, OverlayService::class.java).setAction(action)
    }
}
