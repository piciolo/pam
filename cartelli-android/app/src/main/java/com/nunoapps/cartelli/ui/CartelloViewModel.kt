package com.nunoapps.cartelli.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nunoapps.cartelli.data.off.ProductRepository
import com.nunoapps.cartelli.data.settings.AppSettings
import com.nunoapps.cartelli.data.settings.SettingsRepository
import com.nunoapps.cartelli.domain.CartelloState
import com.nunoapps.cartelli.domain.PriceTag
import com.nunoapps.cartelli.domain.ProductUnit
import com.nunoapps.cartelli.domain.Rounding
import com.nunoapps.cartelli.image.ImageProcessor
import com.nunoapps.cartelli.render.CartelloAssets
import com.nunoapps.cartelli.render.CartelloRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Stato del lookup su Open Food Facts dopo una scansione. */
sealed interface LookupState {
    data object Idle : LookupState
    data object Loading : LookupState
    data class Found(val name: String, val quantityRaw: String?, val imageUrl: String?) : LookupState
    data object NotFound : LookupState
    data class Error(val message: String) : LookupState
}

class CartelloViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val productRepo = ProductRepository()
    private val assets: CartelloAssets by lazy { CartelloAssets.load(getApplication()) }

    private val _state = MutableStateFlow(CartelloState())
    val state: StateFlow<CartelloState> = _state.asStateFlow()

    private val _lookup = MutableStateFlow<LookupState>(LookupState.Idle)
    val lookup: StateFlow<LookupState> = _lookup.asStateFlow()

    private var photoBitmap: Bitmap? = null

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    init {
        // Applica i default dalle impostazioni allo stato iniziale.
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                if (_state.value.footerText.isBlank() ||
                    _state.value == CartelloState()
                ) {
                    _state.value = _state.value.copy(
                        footerText = s.defaultFooter,
                        rounding = s.defaultRounding,
                    )
                }
            }
        }
    }

    // --- mutazioni form ---
    fun setNome(v: String) = update { it.copy(nome = v) }
    fun setPrezzo(v: String) = update { it.copy(prezzo = v) }
    fun setQty(v: String) = update { it.copy(qty = v) }
    fun setUnit(v: ProductUnit) = update { it.copy(unit = v) }
    fun setPriceTag(v: PriceTag) = update { it.copy(priceTag = v) }
    fun setRounding(v: Rounding) = update { it.copy(rounding = v) }
    fun setOfferta(v: Boolean) = update { it.copy(isOfferta = v) }
    fun setShowFooter(v: Boolean) = update { it.copy(showFooter = v) }
    fun setFooterText(v: String) = update { it.copy(footerText = v) }
    fun setPrezzoBase(v: String) = update { it.copy(prezzoBase = v) }

    private inline fun update(block: (CartelloState) -> CartelloState) {
        _state.value = block(_state.value)
    }

    /** Reset per un nuovo cartello (mantiene i default da impostazioni). */
    fun startNew() {
        val s = settings.value
        photoBitmap = null
        _lookup.value = LookupState.Idle
        _state.value = CartelloState(
            footerText = s.defaultFooter,
            rounding = s.defaultRounding,
        )
    }

    /** Chiamato quando la fotocamera legge un barcode. */
    fun onBarcodeScanned(code: String) {
        _lookup.value = LookupState.Loading
        viewModelScope.launch {
            when (val r = productRepo.lookup(code)) {
                is ProductRepository.Result.Found -> {
                    val p = r.product
                    update {
                        it.copy(
                            nome = p.name.ifBlank { it.nome },
                            qty = p.qty.ifBlank { it.qty },
                            unit = if (p.qty.isNotBlank()) p.unit else it.unit,
                            priceTag = suggestTag(p.unit),
                        )
                    }
                    _lookup.value = LookupState.Found(p.name, p.quantityRaw, p.imageUrl)
                }
                is ProductRepository.Result.NotFound -> _lookup.value = LookupState.NotFound
                is ProductRepository.Result.Error -> _lookup.value = LookupState.Error(r.message)
            }
        }
    }

    private fun suggestTag(unit: ProductUnit): PriceTag = when (unit.kind) {
        ProductUnit.Kind.LIQUID -> PriceTag.AL_LT
        ProductUnit.Kind.SOLID -> PriceTag.AL_KG
        ProductUnit.Kind.PIECE -> PriceTag.AL_PZ
    }

    // --- foto prodotto ---
    fun setPhoto(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                ImageProcessor.loadScaled(getApplication(), uri)
            }
            photoBitmap = bmp
            update { it.copy(photoUri = uri.toString()) }
        }
    }

    fun clearPhoto() {
        photoBitmap = null
        update { it.copy(photoUri = null) }
    }

    // --- rendering ---
    fun renderBitmap(outWidth: Int): Bitmap {
        val s = settings.value
        return CartelloRenderer.render(
            outWidth = outWidth,
            state = _state.value,
            assets = assets,
            photo = photoBitmap,
            photoArea = CartelloRenderer.PhotoArea(s.photoXmm, s.photoYmm, s.photoWmm, s.photoHmm),
        )
    }

    // --- impostazioni ---
    fun saveSettings(xMm: Float, yMm: Float, wMm: Float, hMm: Float, footer: String, rounding: Rounding) {
        viewModelScope.launch {
            settingsRepo.updatePhotoArea(xMm, yMm, wMm, hMm)
            settingsRepo.updateDefaultFooter(footer)
            settingsRepo.updateDefaultRounding(rounding)
        }
    }
}
