package dev.ekrankarartma.s3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log
import android.view.View
import android.view.WindowManager
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Tüm ekranı karartan ön plan servisi. Katman TYPE_APPLICATION_OVERLAY ile çizilir.
 * Ton ve yoğunluk [OverlayPrefs]'ten okunur; açıkken değişirse servis yeniden başlatılmaz (ACTION_UPDATE).
 */
class OverlayService : Service() {

    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startOverlay()
            ACTION_STOP -> shutdown()
            ACTION_UPDATE -> applyLook()
            else -> stopSelf()
        }
        // Sistem öldürürse karartma kendiliğinden geri gelmesin; arayüz tercihi gerçek duruma göre düzeltir.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeOverlayView()
        OverlayPrefs.setOverlayOn(this, false)
        requestTileRefresh()
        bildir()
        super.onDestroy()
    }

    private fun startOverlay() {
        // Başlatma isteği artık servis tarafından işleniyor.
        isStartPending = false
        // startForegroundService() ile başlatıldıysa startForeground() 5 saniye içinde çağrılmalı.
        startInForeground()

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Üzerinde gösterme izni yok; servis kapanıyor.")
            shutdown()
            return
        }
        if (overlayView != null) return

        val renk = OverlayPrefs.ton(this).renk
        val view = View(this).apply { setBackgroundColor(renk) }
        try {
            windowManager().addView(view, createOverlayLayoutParams(OverlayPrefs.yogunluk(this).alfa))
        } catch (e: RuntimeException) {
            // WindowManager.BadTokenException, SecurityException vb.
            Log.e(TAG, "Karartma katmanı eklenemedi.", e)
            shutdown()
            return
        }
        overlayView = view
        isRunning = true
        OverlayPrefs.setOverlayOn(this, true)
        requestTileRefresh()
        bildir()
    }

    /** Açık katmanın rengini ve alfasını yerinde değiştirir. Pencere yeniden eklenmez. */
    private fun applyLook() {
        val view = overlayView ?: return
        view.setBackgroundColor(OverlayPrefs.ton(this).renk)
        try {
            windowManager().updateViewLayout(view, createOverlayLayoutParams(OverlayPrefs.yogunluk(this).alfa))
        } catch (e: RuntimeException) {
            Log.w(TAG, "Karartma katmanı güncellenemedi.", e)
        }
    }

    private fun shutdown() {
        isStartPending = false
        removeOverlayView()
        OverlayPrefs.setOverlayOn(this, false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        requestTileRefresh()
        bildir()
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
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
            // Android 10-13: specialUse tipi yok; FOREGROUND_SERVICE_TYPE_NONE, 2 parametreli çağrıyla aynıdır.
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> startForeground(
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
        val closeAction = PendingIntent.getBroadcast(
            this, 1,
            Intent(this, KapatAlici::class.java).setAction(KapatAlici.ACTION_KAPAT),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_karartma)
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

    /**
     * Kutucuk durumunu hemen iter. Yalnızca API 33+ destekler; daha eski sürümlerde genel bir yol yoktur,
     * durum kutucuk paneli açılınca (onStartListening) yenilenir.
     */
    private fun requestTileRefresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TileService.requestListeningState(this, ComponentName(this, QuickTileService::class.java))
        }
    }

    companion object {
        private const val TAG = "OverlayService"
        const val ACTION_START = "dev.ekrankarartma.s3.action.START"
        const val ACTION_STOP = "dev.ekrankarartma.s3.action.STOP"
        const val ACTION_UPDATE = "dev.ekrankarartma.s3.action.UPDATE"
        private const val CHANNEL_ID = "karartma"
        private const val NOTIFICATION_ID = 1

        /** Karartma katmanı gerçekten çizili mi. Süreç öldüğünde bu bayrak da sıfırlanır. */
        @Volatile
        var isRunning: Boolean = false

        /**
         * Başlatma isteği gönderildi, servis henüz işlemedi. Süreç öldüğünde sıfırlanır.
         * Kutucuk ve köprü, açılış sürerken gelen ikinci dokunuşu kapatma olarak yorumlamak için kullanır.
         */
        @Volatile
        internal var isStartPending: Boolean = false

        /** Açık ya da açılıyor. Kutucuk ve köprü açma/kapama kararı için bunu kullanır. */
        val isOnOrStarting: Boolean
            get() = isRunning || isStartPending

        private val dinleyiciler = CopyOnWriteArraySet<() -> Unit>()

        /** Durum değişince çağrılacak dinleyici (ana iş parçacığında). */
        fun dinleyiciEkle(dinleyici: () -> Unit) {
            dinleyiciler.add(dinleyici)
        }

        fun dinleyiciCikar(dinleyici: () -> Unit) {
            dinleyiciler.remove(dinleyici)
        }

        private fun bildir() {
            dinleyiciler.forEach { it() }
        }

        /** Servisi ön plan olarak başlatır. Başarısızsa false döner. */
        fun start(context: Context): Boolean {
            isStartPending = true
            return try {
                context.startForegroundService(intent(context, ACTION_START))
                true
            } catch (e: RuntimeException) {
                Log.e(TAG, "Servis başlatılamadı.", e)
                isStartPending = false
                false
            }
        }

        /**
         * Çalışan servise kapatma isteği gönderir. İstek gönderilemezse false döner; bu durumda katman
         * hâlâ çizili olabilir ve çağıran taraf kayıtlı durumu buna göre bırakmalıdır.
         */
        fun stop(context: Context): Boolean {
            isStartPending = false
            return try {
                context.startService(intent(context, ACTION_STOP))
                true
            } catch (e: RuntimeException) {
                Log.w(TAG, "Servise durdurma isteği gönderilemedi.", e)
                false
            }
        }

        /** Çalışan servisin rengini ve yoğunluğunu günceller. Yalnızca servis çalışıyorken çağrılmalı. */
        fun update(context: Context) {
            try {
                context.startService(intent(context, ACTION_UPDATE))
            } catch (e: RuntimeException) {
                Log.w(TAG, "Servise güncelleme isteği gönderilemedi.", e)
            }
        }

        private fun intent(context: Context, action: String): Intent =
            Intent(context, OverlayService::class.java).setAction(action)
    }
}
