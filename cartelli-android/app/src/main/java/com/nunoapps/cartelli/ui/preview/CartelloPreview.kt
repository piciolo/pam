package com.nunoapps.cartelli.ui.preview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.nunoapps.cartelli.domain.CartelloState
import com.nunoapps.cartelli.data.settings.AppSettings
import com.nunoapps.cartelli.render.CartelloRenderer
import com.nunoapps.cartelli.ui.CartelloViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Anteprima live del cartello: rende il bitmap e lo mostra in proporzioni A4. */
@Composable
fun CartelloPreview(
    vm: CartelloViewModel,
    state: CartelloState,
    settings: AppSettings,
    modifier: Modifier = Modifier,
    renderWidth: Int = 720,
) {
    val bitmap: Bitmap? by produceState<Bitmap?>(initialValue = null, state, settings) {
        value = withContext(Dispatchers.Default) { vm.renderBitmap(renderWidth) }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / CartelloRenderer.ASPECT)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Anteprima cartello",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
