package com.nunoapps.cartelli.domain

import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * Porting 1:1 della logica JavaScript di cartelli.html (funzione render()).
 * Tutta pura e testabile su JVM, senza dipendenze Android.
 */
object PriceCalculator {

    /** Come parseNum() del JS: rimuove i punti (migliaia), virgola -> punto. NaN -> null. */
    fun parseNum(s: String?): Double? {
        if (s.isNullOrBlank()) return null
        val cleaned = s.replace(".", "").replace(",", ".")
        return cleaned.toDoubleOrNull()
    }

    /** Come fmt() del JS: 2 decimali con la virgola. */
    fun fmt(n: Double): String =
        // toFixed(2) arrotonda half-away-from-zero; lo riproduciamo via arrotondamento ai centesimi.
        ((n * 100).roundToLong() / 100.0).let {
            String.format(java.util.Locale.US, "%.2f", it)
        }.replace('.', ',')

    /** Come formatQty() del JS: intero senza decimali, altrimenti virgola. */
    fun formatQty(q: Double): String =
        if (q % 1.0 == 0.0) q.toLong().toString() else q.toString().replace('.', ',')

    fun compute(state: CartelloState): CartelloComputed {
        val nomeUpper = state.nome.uppercase()

        val prezzo = parseNum(state.prezzo)
        val qty = parseNum(state.qty)

        // Prezzone: split di fmt(prezzo) su virgola
        var priceInt = ""
        var priceDec = ""
        val hasPrice = prezzo != null
        if (prezzo != null) {
            val parts = fmt(prezzo).split(",")
            priceInt = parts.getOrElse(0) { "" }
            priceDec = parts.getOrElse(1) { "" }
        }

        // Riga prezzo al litro / al kg
        var unitLine: String? = null
        var info = "Inserisci prezzo e formato."
        if (prezzo != null && qty != null && qty > 0) {
            when (state.unit.kind) {
                ProductUnit.Kind.LIQUID -> {
                    val litres = qty * state.unit.factor
                    val v = roundUnit(prezzo / litres, state.rounding)
                    unitLine = "${state.unit.code} ${formatQty(qty)} - al lt  € ${fmt(v)}"
                    info = "${fmt(prezzo)} € ÷ ${trimNum(litres)} L = ${fmt(v)} €/L"
                }
                ProductUnit.Kind.SOLID -> {
                    val kg = qty * state.unit.factor
                    val k = roundUnit(prezzo / kg, state.rounding)
                    unitLine = "${state.unit.code} ${formatQty(qty)} - al kg  € ${fmt(k)}"
                    info = "${fmt(prezzo)} € ÷ ${trimNum(kg)} kg = ${fmt(k)} €/kg"
                }
                ProductUnit.Kind.PIECE -> {
                    unitLine = null
                    info = "Formato \"pezzo\": nessun prezzo unitario."
                }
            }
        }

        // Box sconto -% (facoltativa)
        var scontoPerc: Int? = null
        val base = parseNum(state.prezzoBase)
        if (base != null && prezzo != null && base > prezzo) {
            val perc = ((base - prezzo) / base * 100).roundToInt()
            scontoPerc = perc
            info += "  |  sconto -$perc% (base ${fmt(base)} €)"
        }

        return CartelloComputed(
            nomeUpper = nomeUpper,
            hasPrice = hasPrice,
            priceInt = priceInt,
            priceDec = priceDec,
            unitLine = unitLine,
            scontoPerc = scontoPerc,
            info = info,
        )
    }

    private fun roundUnit(v: Double, rounding: Rounding): Double = when (rounding) {
        Rounding.CEIL -> ceil(v * 100) / 100.0
        Rounding.ROUND -> (v * 100).roundToLong() / 100.0
    }

    /** Come String(x).replace('.',',') del JS: niente .0 superfluo per gli interi. */
    private fun trimNum(x: Double): String =
        if (x % 1.0 == 0.0) x.toLong().toString() else x.toString().replace('.', ',')

    private fun Double.roundToInt(): Int = Math.round(this).toInt()
}
