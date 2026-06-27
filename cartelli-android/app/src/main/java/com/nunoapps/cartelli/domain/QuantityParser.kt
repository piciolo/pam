package com.nunoapps.cartelli.domain

/**
 * Converte il campo "quantity" di Open Food Facts (es. "250 g", "1,5 L",
 * "6 x 33 cl") nei campi formato+unità usati dal cartello.
 */
object QuantityParser {

    data class Parsed(val qty: String, val unit: ProductUnit)

    private val unitAliases = mapOf(
        "cl" to ProductUnit.CL,
        "ml" to ProductUnit.ML,
        "l" to ProductUnit.L,
        "lt" to ProductUnit.L,
        "litro" to ProductUnit.L,
        "litri" to ProductUnit.L,
        "g" to ProductUnit.G,
        "gr" to ProductUnit.G,
        "grammi" to ProductUnit.G,
        "kg" to ProductUnit.KG,
    )

    // 6 x 33 cl  /  6x33cl  /  6 × 33 cl
    private val packRegex =
        Regex("""(\d+)\s*[x×]\s*(\d+(?:[.,]\d+)?)\s*(kg|cl|ml|lt|gr|g|l)\b""", RegexOption.IGNORE_CASE)

    // 250 g  /  1,5 L  /  33cl
    private val singleRegex =
        Regex("""(\d+(?:[.,]\d+)?)\s*(kg|cl|ml|lt|gr|g|l)\b""", RegexOption.IGNORE_CASE)

    fun parse(raw: String?): Parsed? {
        if (raw.isNullOrBlank()) return null
        val text = raw.trim()

        packRegex.find(text)?.let { m ->
            val count = m.groupValues[1].toDouble()
            val each = m.groupValues[2].replace(',', '.').toDouble()
            val unit = unitAliases[m.groupValues[3].lowercase()] ?: return@let
            val total = count * each
            return Parsed(formatQty(total), unit)
        }

        singleRegex.find(text)?.let { m ->
            val unit = unitAliases[m.groupValues[2].lowercase()] ?: return@let
            return Parsed(m.groupValues[1].replace('.', ','), unit)
        }
        return null
    }

    private fun formatQty(x: Double): String =
        if (x % 1.0 == 0.0) x.toLong().toString() else x.toString().replace('.', ',')
}
