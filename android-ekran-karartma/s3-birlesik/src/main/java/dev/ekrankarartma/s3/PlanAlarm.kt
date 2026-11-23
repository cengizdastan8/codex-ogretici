package dev.ekrankarartma.s3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.ZoneId

/**
 * Plan sınırlarını AlarmManager.setAlarmClock ile kurar.
 *
 * Kanıt: developer.android.com AlarmManager referansı, setAlarmClock bölümü (2026-11-23 tarihinde okundu):
 *   "Alarms scheduled via this API will be allowed to start a foreground service even if the app is in the background."
 *   Aynı bölüm: "Requires Manifest.permission.SCHEDULE_EXACT_ALARM"; targetSdk 31+ için bu izin gerekir.
 * Sınır: arka plan kısıtlarının resmi istisna listesinde alarm maddesi yok; dayanak referans sayfasıdır.
 * Cihazda test edilmeden tam güvenilmemeli. İzin yoksa önce [canSchedule] false döner ve plan kurulmaz.
 *
 * Yan etki: sistem durum çubuğunda alarm simgesi gösterebilir. Simgeye dokunulunca [MainActivity] açılır.
 */
object PlanAlarm {
    const val ACTION_SINIR = "dev.ekrankarartma.s3.action.PLAN_SINIR"
    private const val TAG = "PlanAlarm"
    private const val ISTEK_SINIR = 10
    private const val ISTEK_GOSTER = 11

    private fun alarmManager(ctx: Context): AlarmManager = ctx.getSystemService(AlarmManager::class.java)

    /** Tam zamanlı alarm izni var mı? API 31 öncesinde bu izin gerekmez. */
    fun canSchedule(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager(ctx).canScheduleExactAlarms()
    }

    /** Bir sonraki plan sınırını kurar. İzin yoksa ya da kurulamazsa false döner. */
    fun schedule(ctx: Context): Boolean {
        if (!canSchedule(ctx)) return false
        val (baslangic, bitis) = OverlayPrefs.planPenceresi(ctx)
        val sinir = sonrakiSinir(Saat.simdi(), baslangic, bitis)
        val tetik = sinir.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val gosterme = PendingIntent.getActivity(
            ctx, ISTEK_GOSTER, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val islem = PendingIntent.getBroadcast(
            ctx, ISTEK_SINIR, sinirNiyeti(ctx),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return try {
            alarmManager(ctx).setAlarmClock(AlarmManager.AlarmClockInfo(tetik, gosterme), islem)
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "Plan alarmı kurulamadı.", e)
            false
        }
    }

    fun cancel(ctx: Context) {
        val islem = PendingIntent.getBroadcast(
            ctx, ISTEK_SINIR, sinirNiyeti(ctx),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager(ctx).cancel(islem)
        islem.cancel()
    }

    private fun sinirNiyeti(ctx: Context): Intent =
        Intent(ctx, PlanReceiver::class.java).setAction(ACTION_SINIR)
}
