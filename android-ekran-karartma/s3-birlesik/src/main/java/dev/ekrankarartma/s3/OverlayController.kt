package dev.ekrankarartma.s3

import android.content.Context
import java.time.LocalTime

/**
 * Karartma kararlarının tek merkezi: manuel komutlar, plan sınırları, izin ve alarm uzlaştırması.
 *
 * Manuel kural: plan açıkken kullanıcı açma/kapama seçerse, bu seçim bir sonraki plan sınırına kadar
 * geçerlidir. Pencere içindeyken kapatmak sınıra kadar kapalı tutar; pencere dışındayken açmak sınıra
 * kadar açık tutar.
 */
object OverlayController {

    /** Kullanıcı açma/kapama seçti (ana düğme, hızlı ayar, bildirim). */
    fun manualSet(ctx: Context, acik: Boolean) {
        if (OverlayPrefs.planAcik(ctx)) {
            val (baslangic, bitis) = OverlayPrefs.planPenceresi(ctx)
            OverlayPrefs.setGecersizKilma(ctx, gecersizKilmaOlustur(Saat.simdi(), baslangic, bitis, acik))
        }
        applyNow(ctx, acik)
    }

    /** Servisi istenen duruma getirir. Zaten açıksa yeniden başlatmaz. */
    fun applyNow(ctx: Context, acik: Boolean) {
        if (acik) {
            if (!OverlayService.isRunning) {
                OverlayPrefs.setOverlayOn(ctx, OverlayService.start(ctx))
            }
        } else {
            var halaAcik = false
            if (OverlayService.isRunning || OverlayPrefs.overlayOn(ctx)) {
                // Durdurma isteği gönderilemediyse katman hâlâ çizili olabilir; kayıtlı durum bunu yansıtmalı.
                if (!OverlayService.stop(ctx)) halaAcik = OverlayService.isRunning
            }
            OverlayPrefs.setOverlayOn(ctx, halaAcik)
        }
    }

    /** Ton ya da yoğunluk değişti: açık katman yerinde güncellenir, servis yeniden başlatılmaz. */
    fun refreshLook(ctx: Context) {
        if (OverlayService.isRunning) OverlayService.update(ctx)
    }

    fun planSet(ctx: Context, acik: Boolean) {
        OverlayPrefs.setPlanAcik(ctx, acik)
        planChanged(ctx)
    }

    /** Pencere değişti. Başlangıç ve bitiş aynıysa false döner ve hiçbir şey değişmez. */
    fun planWindowChanged(ctx: Context, baslangic: LocalTime, bitis: LocalTime): Boolean {
        if (baslangic == bitis) return false
        OverlayPrefs.setPlanPenceresi(ctx, baslangic, bitis)
        planChanged(ctx)
        return true
    }

    /** Plan açıldı, kapandı ya da penceresi değişti: manuel tercih sıfırlanır, alarm kurulur, durum uygulanır. */
    fun planChanged(ctx: Context) {
        OverlayPrefs.temizleGecersizKilma(ctx)
        syncPlan(ctx)
        if (OverlayPrefs.planAcik(ctx) && !OverlayPrefs.planBekliyor(ctx)) {
            val (baslangic, bitis) = OverlayPrefs.planPenceresi(ctx)
            applyNow(ctx, istenenDurum(Saat.simdi(), baslangic, bitis, null))
        }
    }

    /**
     * Alarmları plan durumuna göre kurar ya da iptal eder. Karartma durumunu değiştirmez;
     * açılış ve saat değişimi yalnızca bunu kullanır.
     */
    fun syncPlan(ctx: Context) {
        if (!OverlayPrefs.planAcik(ctx)) {
            OverlayPrefs.setPlanBekliyor(ctx, false)
            PlanAlarm.cancel(ctx)
            return
        }
        val kuruldu = PlanAlarm.schedule(ctx)
        OverlayPrefs.setPlanBekliyor(ctx, !kuruldu)
    }

    /** Uygulama öne geldi: alarm izni yeni verildiyse bekleyen plan hemen uygulanır. */
    fun onAppResumed(ctx: Context) {
        val bekliyordu = OverlayPrefs.planBekliyor(ctx)
        syncPlan(ctx)
        if (bekliyordu && OverlayPrefs.planAcik(ctx) && !OverlayPrefs.planBekliyor(ctx)) {
            val (baslangic, bitis) = OverlayPrefs.planPenceresi(ctx)
            applyNow(ctx, istenenDurum(Saat.simdi(), baslangic, bitis, OverlayPrefs.gecersizKilma(ctx)))
        }
    }

    /** Plan alarmı tetiklendi: sınırda durumu uygular ve bir sonraki sınırı kurar. */
    fun onPlanBoundary(ctx: Context) {
        if (!OverlayPrefs.planAcik(ctx)) {
            PlanAlarm.cancel(ctx)
            return
        }
        val simdi = Saat.simdi()
        val (baslangic, bitis) = OverlayPrefs.planPenceresi(ctx)
        val gecersiz = OverlayPrefs.gecersizKilma(ctx)
        if (gecersiz != null && !simdi.isBefore(gecersiz.bitis)) {
            OverlayPrefs.temizleGecersizKilma(ctx)
        }
        applyNow(ctx, istenenDurum(simdi, baslangic, bitis, gecersiz))
        syncPlan(ctx)
    }
}
