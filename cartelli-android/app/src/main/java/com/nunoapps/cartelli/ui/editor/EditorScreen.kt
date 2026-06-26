package com.nunoapps.cartelli.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nunoapps.cartelli.domain.PriceTag
import com.nunoapps.cartelli.domain.ProductUnit
import com.nunoapps.cartelli.domain.Rounding
import com.nunoapps.cartelli.image.PhotoFiles
import com.nunoapps.cartelli.render.PdfExporter
import com.nunoapps.cartelli.ui.CartelloViewModel
import com.nunoapps.cartelli.ui.LookupState
import com.nunoapps.cartelli.ui.preview.CartelloPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    vm: CartelloViewModel,
    onNewScan: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by vm.state.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val lookup by vm.lookup.collectAsStateWithLifecycle()

    val cameraUri = remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.setPhoto(cameraUri.value)
    }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        vm.setPhoto(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cartello") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                actions = {
                    IconButton(onClick = onNewScan) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Nuova scansione")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LookupBanner(lookup)

            CartelloPreview(vm = vm, state = state, settings = settings)

            // Azioni stampa / condivisione
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            val bmp = withContext(Dispatchers.Default) { vm.renderBitmap(2480) }
                            PdfExporter.print(context, bmp)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Text("  Stampa / PDF")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val bmp = withContext(Dispatchers.Default) { vm.renderBitmap(2480) }
                            val file = withContext(Dispatchers.IO) { PdfExporter.writeA4Pdf(context, bmp) }
                            context.startActivity(
                                android.content.Intent.createChooser(
                                    PdfExporter.shareIntent(context, file), "Condividi cartello"
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text("  Condividi")
                }
            }

            // Foto prodotto
            SectionTitle("Foto del prodotto")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val uri = PhotoFiles.newCameraUri(context)
                        cameraUri.value = uri
                        takePicture.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Text("  Scatta")
                }
                OutlinedButton(onClick = { pickImage.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Text("  Galleria")
                }
            }
            if (state.photoUri != null) {
                OutlinedButton(onClick = { vm.clearPhoto() }) { Text("Rimuovi foto") }
            }

            // Campi
            SectionTitle("Dati cartello")
            LabeledTextField("Nome prodotto", state.nome, vm::setNome)
            LabeledTextField("Prezzo di vendita (€)", state.prezzo, vm::setPrezzo, numeric = true)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField(
                    "Formato (quantità)", state.qty, vm::setQty, numeric = true,
                    modifier = Modifier.weight(1f),
                )
                EnumDropdown(
                    "Unità", ProductUnit.entries, state.unit, { it.code }, vm::setUnit,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnumDropdown(
                    "Etichetta prezzo", PriceTag.entries, state.priceTag, { it.label }, vm::setPriceTag,
                    modifier = Modifier.weight(1f),
                )
                EnumDropdown(
                    "Arrotondamento", Rounding.entries, state.rounding,
                    { if (it == Rounding.CEIL) "Per eccesso (PAM)" else "Normale" }, vm::setRounding,
                    modifier = Modifier.weight(1f),
                )
            }

            LabeledTextField(
                "Prezzo base / listino (€) — opzionale", state.prezzoBase, vm::setPrezzoBase,
                numeric = true, placeholder = "es: 1,59 (mostra la box sconto)",
            )

            CheckRow("Offerta — testata rossa (altrimenti verde \"Pam Conviene\")",
                state.isOfferta, vm::setOfferta)
            CheckRow("Mostra scritta in basso", state.showFooter, vm::setShowFooter)
            LabeledTextField("Testo a piè di pagina", state.footerText, vm::setFooterText)
        }
    }
}

@Composable
private fun LookupBanner(lookup: LookupState) {
    when (lookup) {
        is LookupState.Loading -> InfoCard(MaterialTheme.colorScheme.primary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Text("  Ricerca prodotto…", color = Color.White)
            }
        }
        is LookupState.Found -> InfoCard(Color(0xFF2E7D32)) {
            Column {
                Text("Prodotto riconosciuto", color = Color.White, fontWeight = FontWeight.Bold)
                Text(lookup.name, color = Color.White)
                lookup.quantityRaw?.let { Text("Formato: $it", color = Color.White) }
            }
        }
        is LookupState.NotFound -> InfoCard(Color(0xFF8A6D00)) {
            Text("Prodotto non trovato in Open Food Facts. Inserisci i dati a mano.", color = Color.White)
        }
        is LookupState.Error -> InfoCard(MaterialTheme.colorScheme.error) {
            Text("Errore ricerca: ${lookup.message}", color = Color.White)
        }
        LookupState.Idle -> {}
    }
}

@Composable
private fun InfoCard(bg: Color, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        Column(Modifier.padding(12.dp)) { content() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
