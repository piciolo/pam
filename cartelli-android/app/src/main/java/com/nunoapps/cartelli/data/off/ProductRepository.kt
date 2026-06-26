package com.nunoapps.cartelli.data.off

import com.nunoapps.cartelli.domain.ProductUnit
import com.nunoapps.cartelli.domain.QuantityParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Interroga Open Food Facts (database online gratuito) per barcode e restituisce
 * nome + peso normalizzati. Nessuna API key richiesta; serve uno User-Agent
 * identificativo come da policy del progetto OFF.
 */
class ProductRepository(
    private val client: OkHttpClient = defaultClient(),
    private val baseUrl: String = "https://world.openfoodfacts.org",
) {
    private val json = Json { ignoreUnknownKeys = true }

    sealed interface Result {
        data class Found(val product: ProductInfo) : Result
        data object NotFound : Result
        data class Error(val message: String) : Result
    }

    suspend fun lookup(barcode: String): Result = withContext(Dispatchers.IO) {
        val code = barcode.trim()
        if (code.isEmpty()) return@withContext Result.NotFound
        val url = "$baseUrl/api/v2/product/$code.json" +
            "?fields=product_name,product_name_it,product_name_en,brands,quantity,image_url"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        try {
            client.newCall(request).execute().use { resp ->
                if (resp.code == 404) return@withContext Result.NotFound
                if (!resp.isSuccessful) return@withContext Result.Error("HTTP ${resp.code}")
                val body = resp.body?.string().orEmpty()
                val parsed = json.decodeFromString<OffResponse>(body)
                val p = parsed.product ?: return@withContext Result.NotFound
                val name = buildName(p)
                if (name.isBlank() && p.quantity.isNullOrBlank()) return@withContext Result.NotFound
                val q = QuantityParser.parse(p.quantity)
                Result.Found(
                    ProductInfo(
                        barcode = code,
                        name = name,
                        qty = q?.qty ?: "",
                        unit = q?.unit ?: ProductUnit.PZ,
                        quantityRaw = p.quantity,
                        imageUrl = p.imageUrl?.takeIf { it.isNotBlank() },
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Errore di rete")
        }
    }

    private fun buildName(p: OffProduct): String {
        val base = listOf(p.productNameIt, p.productName, p.productNameEn)
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
            .orEmpty()
        val brand = p.brands?.split(",")?.firstOrNull()?.trim().orEmpty()
        return when {
            base.isBlank() -> brand
            brand.isBlank() -> base
            base.contains(brand, ignoreCase = true) -> base
            else -> "$brand $base"
        }.trim()
    }

    companion object {
        const val USER_AGENT = "PamCartelli/1.0 (piciolo2@gmail.com)"

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
