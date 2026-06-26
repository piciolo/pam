package com.nunoapps.cartelli.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nunoapps.cartelli.domain.Rounding
import com.nunoapps.cartelli.ui.CartelloViewModel
import com.nunoapps.cartelli.ui.editor.EnumDropdown
import com.nunoapps.cartelli.ui.editor.LabeledTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: CartelloViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()

    var x by remember(settings) { mutableStateOf(settings.photoXmm.toString()) }
    var y by remember(settings) { mutableStateOf(settings.photoYmm.toString()) }
    var w by remember(settings) { mutableStateOf(settings.photoWmm.toString()) }
    var h by remember(settings) { mutableStateOf(settings.photoHmm.toString()) }
    var footer by remember(settings) { mutableStateOf(settings.defaultFooter) }
    var rounding by remember(settings) { mutableStateOf(settings.defaultRounding) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
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
            Text("Area foto sul cartello (mm, su foglio A4 210×297)",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Decidi dove e quanto grande stampare la foto del prodotto.",
                style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField("X (mm)", x, { x = it }, numeric = true, modifier = Modifier.weight(1f))
                LabeledTextField("Y (mm)", y, { y = it }, numeric = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField("Larghezza (mm)", w, { w = it }, numeric = true, modifier = Modifier.weight(1f))
                LabeledTextField("Altezza (mm)", h, { h = it }, numeric = true, modifier = Modifier.weight(1f))
            }

            Text("Default", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LabeledTextField("Testo a piè di pagina", footer, { footer = it })
            EnumDropdown(
                "Arrotondamento", Rounding.entries, rounding,
                { if (it == Rounding.CEIL) "Per eccesso (PAM)" else "Normale" }, { rounding = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    vm.saveSettings(
                        xMm = x.toFloatOrNull() ?: settings.photoXmm,
                        yMm = y.toFloatOrNull() ?: settings.photoYmm,
                        wMm = w.toFloatOrNull() ?: settings.photoWmm,
                        hMm = h.toFloatOrNull() ?: settings.photoHmm,
                        footer = footer,
                        rounding = rounding,
                    )
                    Toast.makeText(context, "Impostazioni salvate", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salva") }
        }
    }
}
