package com.nunoapps.cartelli.data.off

import com.nunoapps.cartelli.domain.ProductUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Risposta dell'endpoint /api/v2/product/{barcode}.json (campi che ci servono). */
@Serializable
data class OffResponse(
    val product: OffProduct? = null,
)

@Serializable
data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_it") val productNameIt: String? = null,
    @SerialName("product_name_en") val productNameEn: String? = null,
    val brands: String? = null,
    val quantity: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)

/** Dati prodotto normalizzati pronti per precompilare il form del cartello. */
data class ProductInfo(
    val barcode: String,
    val name: String,
    val qty: String,
    val unit: ProductUnit,
    val quantityRaw: String?,
    val imageUrl: String?,
)
