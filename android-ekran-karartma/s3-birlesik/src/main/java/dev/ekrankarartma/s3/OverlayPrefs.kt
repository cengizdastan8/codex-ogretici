package dev.ekrankarartma.s3

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

/**
 * Kalıcı durum ("karartma" SharedPreferences). [overlayOn] son bilinen durumdur;
 * gerçek durum OverlayService.isRunning'dir ve açılışta bununla uzlaştırılır.
 */
object OverlayPrefs {
    private const val DOSYA = "karartma"
    private const val K_ACIK = "acik"
    private const val K_TON = "ton"
    private const val K_YOGUNLUK = "yogunluk"
    private const val K_PLAN = "plan_acik"
    private const val K_PLAN_BEKLIYOR = "plan_bekliyor"
    private const val K_BASLANGIC = "plan_baslangic_dk"
    private const val K_BITIS = "plan_bitis_dk"
    private const val K_GECERSIZ_ACIK = "gecersiz_acik"
    private const val K_GECERSIZ_BITIS = "gecersiz_bitis"

    private val VARSAYILAN_BASLANGIC: LocalTime = LocalTime.of(22, 0)
    private val VARSAYILAN_BITIS: LocalTime = LocalTime.of(7, 0)

    fun overlayOn(ctx: Context): Boolean = prefs(ctx).getBoolean(K_ACIK, false)

    fun setOverlayOn(ctx: Context, acik: Boolean) {
        prefs(ctx).edit().putBoolean(K_ACIK, acik).apply()
    }

    fun ton(ctx: Context): Ton {
        val ad = prefs(ctx).getString(K_TON, null)
        return Ton.values().firstOrNull { it.name == ad } ?: Ton.NOTR
    }

    fun setTon(ctx: Context, ton: Ton) {
        prefs(ctx).edit().putString(K_TON, ton.name).apply()
    }

    fun yogunluk(ctx: Context): Yogunluk {
        val ad = prefs(ctx).getString(K_YOGUNLUK, null)
        return Yogunluk.values().firstOrNull { it.name == ad } ?: Yogunluk.ORTA
    }

    fun setYogunluk(ctx: Context, yogunluk: Yogunluk) {
        prefs(ctx).edit().putString(K_YOGUNLUK, yogunluk.name).apply()
    }

    fun planAcik(ctx: Context): Boolean = prefs(ctx).getBoolean(K_PLAN, false)

    fun setPlanAcik(ctx: Context, acik: Boolean) {
        prefs(ctx).edit().putBoolean(K_PLAN, acik).apply()
    }

    /** Plan açık ama alarm izni yok: plan, izin verilene kadar uygulanamaz. */
    fun planBekliyor(ctx: Context): Boolean = prefs(ctx).getBoolean(K_PLAN_BEKLIYOR, false)

    fun setPlanBekliyor(ctx: Context, bekliyor: Boolean) {
        prefs(ctx).edit().putBoolean(K_PLAN_BEKLIYOR, bekliyor).apply()
    }

    fun planPenceresi(ctx: Context): Pair<LocalTime, LocalTime> {
        val p = prefs(ctx)
        return Pair(
            dakikadanSaate(p.getInt(K_BASLANGIC, saatiDakikaya(VARSAYILAN_BASLANGIC))),
            dakikadanSaate(p.getInt(K_BITIS, saatiDakikaya(VARSAYILAN_BITIS)))
        )
    }

    fun setPlanPenceresi(ctx: Context, baslangic: LocalTime, bitis: LocalTime) {
        prefs(ctx).edit()
            .putInt(K_BASLANGIC, saatiDakikaya(baslangic))
            .putInt(K_BITIS, saatiDakikaya(bitis))
            .apply()
    }

    fun gecersizKilma(ctx: Context): ManuelGecersizKilma? {
        val p = prefs(ctx)
        val bitisMetni = p.getString(K_GECERSIZ_BITIS, null) ?: return null
        return try {
            ManuelGecersizKilma(p.getBoolean(K_GECERSIZ_ACIK, false), LocalDateTime.parse(bitisMetni))
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun setGecersizKilma(ctx: Context, gecersiz: ManuelGecersizKilma) {
        prefs(ctx).edit()
            .putBoolean(K_GECERSIZ_ACIK, gecersiz.acik)
            .putString(K_GECERSIZ_BITIS, gecersiz.bitis.toString())
            .apply()
    }

    fun temizleGecersizKilma(ctx: Context) {
        prefs(ctx).edit().remove(K_GECERSIZ_ACIK).remove(K_GECERSIZ_BITIS).apply()
    }

    private fun saatiDakikaya(saat: LocalTime): Int = saat.hour * 60 + saat.minute

    private fun dakikadanSaate(dakika: Int): LocalTime = LocalTime.of(dakika / 60, dakika % 60)

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(DOSYA, Context.MODE_PRIVATE)
}
