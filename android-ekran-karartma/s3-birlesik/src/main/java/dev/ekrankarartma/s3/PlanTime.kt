package dev.ekrankarartma.s3

import java.time.LocalDateTime
import java.time.LocalTime

/** Saat kaynağı. Testler [simdi] değerini sabit bir değere ayarlayabilir. */
object Saat {
    @Volatile
    var simdi: () -> LocalDateTime = { LocalDateTime.now() }
}

/** Manuel tercih: [acik] durumu [bitis] anına (bir sonraki plan sınırı) kadar geçerlidir. */
data class ManuelGecersizKilma(val acik: Boolean, val bitis: LocalDateTime)

/**
 * [an] saati [baslangic, bitis) aralığında mı? Gece yarısını aşan aralık desteklenir (22:00-07:00).
 * baslangic == bitis boş aralıktır; hiçbir an bu aralığın içinde değildir.
 */
fun saatAraligindaMi(an: LocalTime, baslangic: LocalTime, bitis: LocalTime): Boolean = when {
    baslangic == bitis -> false
    baslangic < bitis -> !an.isBefore(baslangic) && an.isBefore(bitis)
    else -> !an.isBefore(baslangic) || an.isBefore(bitis)
}

/** [simdi] anından kesin sonra gelen ilk plan sınırı (başlangıç veya bitiş), yerel tarih-saat olarak. */
fun sonrakiSinir(simdi: LocalDateTime, baslangic: LocalTime, bitis: LocalTime): LocalDateTime {
    val bugun = simdi.toLocalDate()
    val yarin = bugun.plusDays(1)
    return listOf(
        bugun.atTime(baslangic),
        bugun.atTime(bitis),
        yarin.atTime(baslangic),
        yarin.atTime(bitis)
    ).filter { it.isAfter(simdi) }
        .reduce { en, aday -> if (aday.isBefore(en)) aday else en }
}

/**
 * Plan açıkken istenen karartma durumu. Geçerli bir manuel tercih varsa o kazanır;
 * tercih yoksa veya bitiş anına ulaşmışsa saat aralığına bakılır.
 */
fun istenenDurum(
    simdi: LocalDateTime,
    baslangic: LocalTime,
    bitis: LocalTime,
    gecersiz: ManuelGecersizKilma?
): Boolean {
    if (gecersiz != null && simdi.isBefore(gecersiz.bitis)) return gecersiz.acik
    return saatAraligindaMi(simdi.toLocalTime(), baslangic, bitis)
}

/** Kullanıcı şimdi [acik] seçtiğinde, bir sonraki plan sınırına kadar geçerli kural. */
fun gecersizKilmaOlustur(
    simdi: LocalDateTime,
    baslangic: LocalTime,
    bitis: LocalTime,
    acik: Boolean
): ManuelGecersizKilma = ManuelGecersizKilma(acik, sonrakiSinir(simdi, baslangic, bitis))
