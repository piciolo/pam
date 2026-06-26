package com.nunoapps.cartelli.render

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import java.io.File
import java.io.FileOutputStream

/**
 * Esporta il cartello come PDF A4 e gestisce condivisione e stampa.
 * Il bitmap in ingresso deve avere proporzioni A4 (vedi CartelloRenderer.ASPECT).
 */
object PdfExporter {

    private const val A4_W_PT = 595 // 210 mm @ 72 dpi
    private const val A4_H_PT = 842 // 297 mm @ 72 dpi

    /** Scrive un PDF A4 a pagina piena (full-bleed) in cache e ne restituisce il File. */
    fun writeA4Pdf(context: Context, bitmap: Bitmap, fileName: String = "cartello.pdf"): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_W_PT, A4_H_PT, 1).create()
        val page = doc.startPage(pageInfo)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply { isAntiAlias = true }
        page.canvas.drawBitmap(
            bitmap, null, RectF(0f, 0f, A4_W_PT.toFloat(), A4_H_PT.toFloat()), paint
        )
        doc.finishPage(page)

        val dir = File(context.cacheDir, "cartelli").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Intent per condividere il PDF (WhatsApp, email, ecc.). */
    fun shareIntent(context: Context, file: File): Intent {
        val uri = uriFor(context, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Apre il dialog di stampa di sistema (con opzione "Salva come PDF"), equivalente
     * a window.print() della pagina web. Stampa il bitmap del cartello a pagina piena.
     */
    fun print(context: Context, bitmap: Bitmap, jobName: String = "Cartello PAM") {
        val helper = PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            colorMode = PrintHelper.COLOR_MODE_COLOR
            orientation = PrintHelper.ORIENTATION_PORTRAIT
        }
        helper.printBitmap(jobName, bitmap)
    }
}
