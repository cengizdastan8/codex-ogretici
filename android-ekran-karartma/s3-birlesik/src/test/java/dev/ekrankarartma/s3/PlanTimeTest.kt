package dev.ekrankarartma.s3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

/** Saf saat hesabı: aralık, sonraki sınır ve manuel gecersiz kılma kuralı. Context gerektirmez. */
class PlanTimeTest {

    private val gece22 = LocalTime.of(22, 0)
    private val sabah7 = LocalTime.of(7, 0)

    private fun t(saat: Int, dakika: Int = 0): LocalTime = LocalTime.of(saat, dakika)

    /** 2026-11-23 tarihinde verilen saat. */
    private fun gun23(saat: Int, dakika: Int = 0): LocalDateTime =
        LocalDateTime.of(2026, 11, 23, saat, dakika)

    /** 2026-11-24 tarihinde verilen saat. */
    private fun gun24(saat: Int, dakika: Int = 0): LocalDateTime =
        LocalDateTime.of(2026, 11, 24, saat, dakika)

    @Test
    fun gecaYarisiniAsanAralikDogruHesaplanir() {
        assertTrue(saatAraligindaMi(t(22), gece22, sabah7))
        assertTrue(saatAraligindaMi(t(23, 30), gece22, sabah7))
        assertTrue(saatAraligindaMi(t(0), gece22, sabah7))
        assertTrue(saatAraligindaMi(t(6, 59), gece22, sabah7))
        assertFalse(saatAraligindaMi(t(7), gece22, sabah7))
        assertFalse(saatAraligindaMi(t(12), gece22, sabah7))
        assertFalse(saatAraligindaMi(t(21, 59), gece22, sabah7))
    }

    @Test
    fun gunIciAralikDogruHesaplanir() {
        val baslangic = t(9)
        val bitis = t(17)
        assertTrue(saatAraligindaMi(t(9), baslangic, bitis))
        assertTrue(saatAraligindaMi(t(16, 59), baslangic, bitis))
        assertFalse(saatAraligindaMi(t(17), baslangic, bitis))
        assertFalse(saatAraligindaMi(t(8, 59), baslangic, bitis))
    }

    @Test
    fun ayniBaslangicVeBitisBosAralikTir() {
        assertFalse(saatAraligindaMi(t(22), t(22), t(22)))
        assertFalse(saatAraligindaMi(t(3), t(22), t(22)))
    }

    @Test
    fun sonrakiSinirGeceYarisiniAsarak() {
        assertEquals(gun23(22), sonrakiSinir(gun23(12), gece22, sabah7))
        assertEquals(gun24(7), sonrakiSinir(gun23(23), gece22, sabah7))
        assertEquals(gun23(7), sonrakiSinir(gun23(6), gece22, sabah7))
        // Tam sınır anı "şimdi" sayılmaz; bir sonraki sınır alınır.
        assertEquals(gun23(22), sonrakiSinir(gun23(7), gece22, sabah7))
        assertEquals(gun24(7), sonrakiSinir(gun23(22), gece22, sabah7))
        assertEquals(gun24(7), sonrakiSinir(LocalDateTime.of(2026, 11, 23, 23, 59, 59), gece22, sabah7))
    }

    @Test
    fun pencereIcindeManuelKapatmaBitiseKadarKapaliTutar() {
        val gecersiz = gecersizKilmaOlustur(gun23(23), gece22, sabah7, acik = false)

        assertEquals(gun24(7), gecersiz.bitis)
        // Pencere içinde (23:30) ama kullanıcı kapattı: kapalı kalır.
        assertFalse(istenenDurum(gun23(23, 30), gece22, sabah7, gecersiz))
        assertFalse(istenenDurum(gun24(6, 59), gece22, sabah7, gecersiz))
        // Bitiş anında geçersiz kılma sona erer; plan 07:00'de kapalı, 22:00'de açık olur.
        assertFalse(istenenDurum(gun24(7), gece22, sabah7, gecersiz))
        assertTrue(istenenDurum(gun24(22), gece22, sabah7, gecersiz))
    }

    @Test
    fun pencereDisindaManuelAcmaBitiseKadarAcikTutar() {
        val gecersiz = gecersizKilmaOlustur(gun23(10), gece22, sabah7, acik = true)

        assertEquals(gun23(22), gecersiz.bitis)
        assertTrue(istenenDurum(gun23(10), gece22, sabah7, gecersiz))
        assertTrue(istenenDurum(gun23(21, 59), gece22, sabah7, gecersiz))
        // Gündüz aralığıyla ayrımı görmek için: 09-17 penceresinde açık tercih 17:00'de sona erer.
        val gunduz = LocalTime.of(9, 0)
        val gunduzBitis = LocalTime.of(17, 0)
        val gunduzTercih = gecersizKilmaOlustur(gun23(10), gunduz, gunduzBitis, acik = true)
        assertTrue(istenenDurum(gun23(16, 59), gunduz, gunduzBitis, gunduzTercih))
        assertFalse(istenenDurum(gun23(17), gunduz, gunduzBitis, gunduzTercih))
    }

    @Test
    fun tercihYokkenSaatAraligiBelirler() {
        assertTrue(istenenDurum(gun23(23), gece22, sabah7, null))
        assertFalse(istenenDurum(gun23(12), gece22, sabah7, null))
    }
}
