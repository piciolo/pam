package com.nunoapps.cartelli.domain

/** Unità di misura del formato prodotto (stessi valori della select di cartelli.html). */
enum class ProductUnit(val code: String, val factor: Double, val kind: Kind) {
    CL("cl", 0.01, Kind.LIQUID),
    ML("ml", 0.001, Kind.LIQUID),
    L("l", 1.0, Kind.LIQUID),
    G("g", 0.001, Kind.SOLID),
    KG("kg", 1.0, Kind.SOLID),
    PZ("pz", 0.0, Kind.PIECE);

    enum class Kind { LIQUID, SOLID, PIECE }

    companion object {
        fun fromCode(code: String?): ProductUnit =
            entries.firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) } ?: PZ
    }
}

/** Etichetta accanto al prezzone. */
enum class PriceTag(val label: String) {
    AL_PZ("AL PZ"), AL_KG("AL KG"), AL_LT("AL LT"), CAD("CAD")
}

/** Tipo di arrotondamento del prezzo unitario. */
enum class Rounding { CEIL, ROUND }

/**
 * Stato modificabile del cartello. I valori numerici sono tenuti come testo grezzo
 * (es. "1,19") esattamente come negli input della pagina web, e convertiti al volo.
 */
data class CartelloState(
    val nome: String = "",
    val prezzo: String = "",
    val qty: String = "",
    val unit: ProductUnit = ProductUnit.PZ,
    val priceTag: PriceTag = PriceTag.AL_PZ,
    val rounding: Rounding = Rounding.CEIL,
    val isOfferta: Boolean = true,
    val showFooter: Boolean = true,
    val footerText: String = "per questo prodotto rivolgersi alla cassa",
    val prezzoBase: String = "",
    val photoUri: String? = null,
)

/**
 * Risultato del calcolo, pronto per il rendering. [priceInt]/[priceDec] sono le due
 * parti del prezzone; [unitLine] è la riga "al lt/al kg" (null se non applicabile);
 * [scontoPerc] è la percentuale della box sconto (null se assente).
 */
data class CartelloComputed(
    val nomeUpper: String,
    val hasPrice: Boolean,
    val priceInt: String,
    val priceDec: String,
    val unitLine: String?,
    val scontoPerc: Int?,
    val info: String,
)
