package com.nunoapps.cartelli.ui.scan

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.nunoapps.cartelli.camera.BarcodeAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onBarcode: (String) -> Unit,
    onManual: () -> Unit,
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    Box(Modifier.fillMaxSize()) {
        if (cameraPermission.status.isGranted) {
            CameraPreview(onBarcode = onBarcode, modifier = Modifier.fillMaxSize())
            ScanOverlay(onManual = onManual, modifier = Modifier.fillMaxSize())
        } else {
            PermissionRequest(
                onRequest = { cameraPermission.launchPermissionRequest() },
                onManual = onManual,
            )
        }
    }
}

@Composable
private fun CameraPreview(onBarcode: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(analysisExecutor, BarcodeAnalyzer(onBarcode)) }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                )
            } catch (_: Exception) {
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
            analysisExecutor.shutdown()
        }
    }
}

@Composable
private fun ScanOverlay(onManual: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Inquadra il codice a barre del prodotto",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )
        OutlinedButton(onClick = onManual, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Text("  Inserisci a mano", color = Color.White)
        }
    }
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit, onManual: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Serve il permesso fotocamera per leggere i codici a barre.",
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRequest, modifier = Modifier.padding(top = 16.dp)) {
            Text("Consenti fotocamera")
        }
        OutlinedButton(onClick = onManual, modifier = Modifier.padding(top = 8.dp)) {
            Text("Inserisci a mano")
        }
    }
}
