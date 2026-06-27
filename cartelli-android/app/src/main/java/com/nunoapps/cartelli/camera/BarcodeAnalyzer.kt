package com.nunoapps.cartelli.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Analizzatore CameraX che legge i codici a barre dei prodotti con ML Kit (on-device).
 * Chiama [onBarcode] una sola volta con il primo codice valido letto.
 */
class BarcodeAnalyzer(
    private val onBarcode: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    @Volatile private var done = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (done) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstNotNullOfOrNull { it.rawValueForProduct() }
                if (value != null && !done) {
                    done = true
                    onBarcode(value)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    /** Accetta solo formati tipici dei prodotti (EAN/UPC), ignora QR e simili. */
    private fun Barcode.rawValueForProduct(): String? {
        val v = rawValue ?: return null
        return when (format) {
            Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_ITF, Barcode.FORMAT_CODE_128 -> v
            else -> null
        }
    }
}
