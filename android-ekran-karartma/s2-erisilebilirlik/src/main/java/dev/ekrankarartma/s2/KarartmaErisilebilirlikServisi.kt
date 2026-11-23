package dev.ekrankarartma.s2

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

/**
 * Karartma katmani, bu servisin icinden TYPE_ACCESSIBILITY_OVERLAY ile cizilir.
 * SYSTEM_ALERT_WINDOW izni gerekmez.
 *
 * Servis yalnizca sistem tarafindan baglandiginda calisir. Baglanti kesilince
 * katman kaldirilir ve bildirim iptal edilir. Pil optimizasyonu servisi durdurabilir.
 *
 * Karartma kendiliginden geri gelmez: surec olur, yeniden baslatma veya baglanti kesilmesi
 * sonrasi servis yeniden baglandiginda onceki "acik" tercihi uygulanmaz; tercih kapali yazilir.
 */
class KarartmaErisilebilirlikServisi : AccessibilityService() {

    private var katman: KarartmaKatmani? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        baglandi()
    }

    /** Baglanti kurulunca calisir. onServiceConnected'dan ve birim testlerinden cagrilir. */
    internal fun baglandi() {
        ornek = this
        katman = KarartmaKatmani(getSystemService(WindowManager::class.java), this)
        // Onceki surecten kalan "acik" tercih geri yuklenmez. Katman ve bildirim kapali baslar,
        // bu da onceki bildirimi ve kutucuk durumunu da duzeltir.
        karartmayiAyarla(false)
    }

    /** Karartma katmani su an ekranda mi. Test ve tanilama icin. */
    internal val karartmaAcikMi: Boolean
        get() = katman?.gorunuyorMu == true

    /**
     * Karartmayi acar veya kapatir. Tercih, katman, bildirim ve kutucugu birlikte gunceller.
     * Dondurur: karartma gercekten acik mi (katman eklenebildi mi).
     */
    fun karartmayiAyarla(acik: Boolean): Boolean {
        val k = katman
        val gercekAcik = if (acik && k != null) {
            k.goster()
        } else {
            k?.kaldir()
            false
        }
        KarartmaDurumu.tercihYaz(this, gercekAcik)
        if (gercekAcik) KarartmaBildirimi.goster(this) else KarartmaBildirimi.iptal(this)
        KarartmaDurumu.kutucuguGuncelle(this)
        return gercekAcik
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Olay dinlenmez: XML'de olay tipi tanimli degildir, bu metot cagrilmamali.
    }

    override fun onInterrupt() {
        // Geri bildirim sesli veya titresimli degildir; kesilecek bir islem yoktur.
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // Servis kapandi (kullanici kapatti ya da sistem durdurdu): karartma da kapanir.
        // Tercih senkron yazilir (KarartmaDurumu.tercihYaz), surec hemen sonra olse de kaybolmaz.
        KarartmaDurumu.tercihYaz(this, false)
        temizle()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        temizle()
        super.onDestroy()
    }

    /** Katmani kaldirir ve statik ornegi birakir. onUnbind ve onDestroy'dan cagrilir. */
    private fun temizle() {
        katman?.kaldir()
        katman = null
        if (ornek === this) ornek = null
        KarartmaBildirimi.iptal(this)
        KarartmaDurumu.kutucuguGuncelle(this)
    }

    companion object {
        /** Servis sistem tarafindan bagliyken bu alan doludur. Statik baglanti bayragi budur. */
        @Volatile
        internal var ornek: KarartmaErisilebilirlikServisi? = null
            private set

        /** Servis bagli mi (statik bayrak). */
        val bagli: Boolean
            get() = ornek != null
    }
}
