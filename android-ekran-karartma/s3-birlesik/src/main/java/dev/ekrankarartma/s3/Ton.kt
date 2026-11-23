package dev.ekrankarartma.s3

/** Karartma rengi. Renkler sabittir; yoğunluk alfayla ayrıca verilir. */
enum class Ton(val renk: Int, val etiketKimligi: Int) {
    NOTR(0xFF000000.toInt(), R.string.ton_notr),
    SICAK(0xFF3D2600.toInt(), R.string.ton_sicak),
    MAT(0xFF303030.toInt(), R.string.ton_mat)
}
