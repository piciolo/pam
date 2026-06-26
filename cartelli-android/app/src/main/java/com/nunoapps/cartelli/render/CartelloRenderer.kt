package com.nunoapps.cartelli.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.nunoapps.cartelli.domain.CartelloComputed
import com.nunoapps.cartelli.domain.CartelloState
import com.nunoapps.cartelli.domain.PriceCalculator

/**
 * Disegna il cartello PAM su un [Canvas], riproducendo il layout di cartelli.html.
 * Coordinate di riferimento in px CSS @96dpi: A4 = 793.7 x 1122.5 (gli stessi
 * valori px usati dal CSS). Tutto viene scalato su [outWidth].
 *
 * Sorgente unica usata sia dall'anteprima a schermo sia dall'export PDF.
 */
object CartelloRenderer {

    const val BASE_W = 793.7f   // 210 mm @ 96 dpi
    const val BASE_H = 1122.52f // 297 mm @ 96 dpi
    const val ASPECT = BASE_H / BASE_W
    private const val MM_TO_CSS = 96f / 25.4f

    private const val PRICE_RED = 0xFFFF0508.toInt()
    private const val SHADOW = 0xFF111111.toInt()
    private const val PAM_RED = 0xFFE3031B.toInt()

    /** Area (in mm sul foglio A4) dove va stampata la foto del prodotto. */
    data class PhotoArea(val xMm: Float, val yMm: Float, val wMm: Float, val hMm: Float)

    fun outHeight(outWidth: Int): Int = (outWidth * ASPECT).toInt()

    fun render(
        outWidth: Int,
        state: CartelloState,
        assets: CartelloAssets,
        photo: Bitmap?,
        photoArea: PhotoArea,
        computed: CartelloComputed = PriceCalculator.compute(state),
    ): Bitmap {
        val w = outWidth
        val h = outHeight(outWidth)
        val s = w / BASE_W // fattore di scala px CSS -> px output
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        c.drawColor(Color.WHITE)

        // --- Banner testata (Offerta rossa / Pam Conviene verde) ---
        val banner = if (state.isOfferta) assets.bannerOfferta else assets.bannerConviene
        val bannerH = w * banner.height.toFloat() / banner.width.toFloat()
        c.drawBitmap(banner, null, RectF(0f, 0f, w.toFloat(), bannerH), filterPaint())

        // --- Logo PAM centrato ---
        val logoH = 74f * s
        val logoW = logoH * assets.logo.width / assets.logo.height
        val logoTop = bannerH + 14f * s
        c.drawBitmap(
            assets.logo, null,
            RectF((w - logoW) / 2f, logoTop, (w + logoW) / 2f, logoTop + logoH), filterPaint()
        )

        // --- Foto prodotto (sotto al testo, nell'area configurata) ---
        photo?.let { drawPhotoContain(c, it, photoArea, s) }

        // --- Nome prodotto (uppercase, a capo automatico) ---
        var cursorY = logoTop + logoH + 46f * s
        val nomePaint = textPaint(assets.pamText, 50f * s, Color.BLACK).apply {
            letterSpacing = -1f / 50f
        }
        val nomeMaxW = w - 76f * s
        val lines = wrapText(computed.nomeUpper, nomePaint, nomeMaxW)
        val nomeLineH = 1.04f * 50f * s
        val fmNome = nomePaint.fontMetrics
        var nomeBottom = cursorY
        for (line in lines) {
            val baseline = cursorY - fmNome.ascent
            c.drawText(line, 38f * s, baseline, nomePaint)
            cursorY += nomeLineH
            nomeBottom = cursorY
        }
        // min-height 58px
        nomeBottom = maxOf(nomeBottom, logoTop + logoH + 46f * s + 58f * s)

        // --- Riga "al lt / al kg" ---
        if (computed.unitLine != null) {
            val unitPaint = textPaint(assets.pamText, 40f * s, Color.BLACK)
            val top = nomeBottom + 30f * s
            c.drawText(computed.unitLine, 38f * s, top - unitPaint.fontMetrics.ascent, unitPaint)
            cursorY = top + 40f * s
        } else {
            cursorY = nomeBottom
        }

        // --- Box sconto -% (facoltativa) ---
        computed.scontoPerc?.let { perc ->
            val scPaint = textPaint(assets.pamPrice, 66f * s, Color.WHITE)
            val txt = "-$perc%"
            val padX = 30f * s
            val padTop = 6f * s
            val padBottom = 12f * s
            val fm = scPaint.fontMetrics
            val txtW = scPaint.measureText(txt)
            val top = cursorY + 26f * s
            val boxLeft = 38f * s
            val boxH = (fm.descent - fm.ascent) + padTop + padBottom
            val boxRect = RectF(boxLeft, top, boxLeft + txtW + padX * 2, top + boxH)
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PAM_RED }
            c.drawRoundRect(boxRect, 12f * s, 12f * s, bg)
            c.drawText(txt, boxLeft + padX, top + padTop - fm.ascent, scPaint)
        }

        // --- Blocco prezzone in basso a destra ---
        drawPriceBlock(c, computed, state.priceTag.label, assets, s, w, h)

        // --- Footer in corsivo ---
        if (state.showFooter && state.footerText.isNotBlank()) {
            val fPaint = textPaint(assets.pamText, 27f * s, Color.BLACK).apply {
                textSkewX = -0.22f // finto corsivo
                textAlign = Paint.Align.CENTER
            }
            val baseline = h - 50f * s
            c.drawText(state.footerText, w / 2f, baseline, fPaint)
        }

        return bitmap
    }

    private fun drawPriceBlock(
        c: Canvas, computed: CartelloComputed, tagLabel: String, assets: CartelloAssets,
        s: Float, w: Int, h: Int,
    ) {
        if (!computed.hasPrice) return
        val blockRight = w - 48f * s
        val blockBottom = h - 120f * s

        // Paint colonna destra (decimali, EURO, etichetta)
        val decDigitsPaint = pricePaint(assets.pamPrice, 165f * s)
        val commaPaint = pricePaint(assets.pamPrice, 0.55f * 165f * s)
        val euroPaint = pricePaint(assets.pamPrice, 78f * s).apply { letterSpacing = 1f / 78f }
        val tagPaint = textPaint(assets.pamText, 54f * s, Color.BLACK).apply { letterSpacing = 1f / 54f }

        val digitsW = decDigitsPaint.measureText(computed.priceDec)
        val commaW = commaPaint.measureText(",")
        val decW = digitsW + commaW
        val euroW = euroPaint.measureText("EURO")
        val tagW = tagPaint.measureText(tagLabel)
        val columnW = maxOf(decW, euroW, tagW)

        val intPaint = pricePaint(assets.pamPrice, 370f * s)
        val intW = intPaint.measureText(computed.priceInt)

        val intRight = blockRight - columnW - 30f * s
        val intLeft = intRight - intW

        // Altezza del blocco = max(glifo intero, colonna)
        val fmInt = intPaint.fontMetrics
        val intGlyphH = fmInt.descent - fmInt.ascent
        val decBoxH = 0.8f * 165f * s
        val euroBoxH = 78f * s
        val tagBoxH = 1.2f * 54f * s
        val columnH = 4f * s + decBoxH + 18f * s + euroBoxH + 20f * s + tagBoxH
        val blockTop = blockBottom - maxOf(intGlyphH, columnH)

        // Intero (tops allineati a blockTop)
        drawTextShadow(c, computed.priceInt, intLeft, blockTop - fmInt.ascent, intPaint, 8f * s, 9f * s)

        // Decimali: ",19" allineati a destra su blockRight
        val decTop = blockTop + 4f * s
        val fmDec = decDigitsPaint.fontMetrics
        val decBaseline = decTop - fmDec.ascent
        val digitsX = blockRight - digitsW
        val commaX = digitsX - commaW
        drawTextShadow(c, ",", commaX, decBaseline, commaPaint, 6f * s, 7f * s)
        drawTextShadow(c, computed.priceDec, digitsX, decBaseline, decDigitsPaint, 6f * s, 7f * s)

        // EURO
        val euroTop = decTop + decBoxH + 18f * s
        val fmEuro = euroPaint.fontMetrics
        drawTextShadow(c, "EURO", blockRight - euroW, euroTop - fmEuro.ascent, euroPaint, 4f * s, 5f * s)

        // Etichetta (AL PZ / AL KG / ...)
        val tagTop = euroTop + euroBoxH + 20f * s
        val fmTag = tagPaint.fontMetrics
        c.drawText(tagLabel, blockRight - tagW, tagTop - fmTag.ascent, tagPaint)
    }

    /** Disegna la foto contenuta nell'area (preserva proporzioni, centrata). */
    private fun drawPhotoContain(c: Canvas, photo: Bitmap, area: PhotoArea, s: Float) {
        val left = area.xMm * MM_TO_CSS * s
        val top = area.yMm * MM_TO_CSS * s
        val maxW = area.wMm * MM_TO_CSS * s
        val maxH = area.hMm * MM_TO_CSS * s
        if (maxW <= 0 || maxH <= 0) return
        val scale = minOf(maxW / photo.width, maxH / photo.height)
        val dw = photo.width * scale
        val dh = photo.height * scale
        val dx = left + (maxW - dw) / 2f
        val dy = top + (maxH - dh) / 2f
        c.drawBitmap(photo, null, RectF(dx, dy, dx + dw, dy + dh), filterPaint())
    }

    // --- helper paint ---
    private fun filterPaint() = Paint(Paint.FILTER_BITMAP_FLAG).apply { isAntiAlias = true }

    private fun textPaint(tf: Typeface, size: Float, color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = tf
        textSize = size
        this.color = color
        isSubpixelText = true
    }

    private fun pricePaint(tf: Typeface, size: Float) = textPaint(tf, size, PRICE_RED)

    private fun drawTextShadow(
        c: Canvas, text: String, x: Float, baseline: Float, paint: Paint, dx: Float, dy: Float,
    ) {
        val shadow = Paint(paint)
        shadow.color = SHADOW
        c.drawText(text, x + dx, baseline + dy, shadow)
        c.drawText(text, x, baseline, paint)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth || current.isEmpty()) {
                current = StringBuilder(candidate)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}
