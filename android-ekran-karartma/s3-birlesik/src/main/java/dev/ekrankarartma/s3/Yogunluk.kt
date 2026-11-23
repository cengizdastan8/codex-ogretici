package dev.ekrankarartma.s3

/** Karartma yoğunluğu. Alfa, üst sınır [MAX_ALPHA] ile sınırlıdır. */
enum class Yogunluk(val alfa: Float, val etiketKimligi: Int) {
    HAFIF(0.35f, R.string.yogunluk_hafif),
    ORTA(0.55f, R.string.yogunluk_orta),
    GUCLU(MAX_ALPHA, R.string.yogunluk_guclu)
}
