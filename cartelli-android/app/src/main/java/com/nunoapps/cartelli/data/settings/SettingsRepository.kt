package com.nunoapps.cartelli.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nunoapps.cartelli.domain.Rounding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cartelli_settings")

/**
 * Posizione e dimensione dell'area in cui viene stampata la foto del prodotto sul
 * cartello, in millimetri sul foglio A4 (le "misure che decido io"), più i default
 * di footer e arrotondamento.
 */
data class AppSettings(
    val photoXmm: Float = 12f,
    val photoYmm: Float = 110f,
    val photoWmm: Float = 95f,
    val photoHmm: Float = 95f,
    val defaultFooter: String = "per questo prodotto rivolgersi alla cassa",
    val defaultRounding: Rounding = Rounding.CEIL,
)

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            photoXmm = p[KEY_X] ?: 12f,
            photoYmm = p[KEY_Y] ?: 110f,
            photoWmm = p[KEY_W] ?: 95f,
            photoHmm = p[KEY_H] ?: 95f,
            defaultFooter = p[KEY_FOOTER] ?: "per questo prodotto rivolgersi alla cassa",
            defaultRounding = runCatching { Rounding.valueOf(p[KEY_ROUNDING] ?: "CEIL") }
                .getOrDefault(Rounding.CEIL),
        )
    }

    suspend fun updatePhotoArea(xMm: Float, yMm: Float, wMm: Float, hMm: Float) {
        context.dataStore.edit {
            it[KEY_X] = xMm; it[KEY_Y] = yMm; it[KEY_W] = wMm; it[KEY_H] = hMm
        }
    }

    suspend fun updateDefaultFooter(footer: String) {
        context.dataStore.edit { it[KEY_FOOTER] = footer }
    }

    suspend fun updateDefaultRounding(rounding: Rounding) {
        context.dataStore.edit { it[KEY_ROUNDING] = rounding.name }
    }

    companion object {
        private val KEY_X = floatPreferencesKey("photo_x_mm")
        private val KEY_Y = floatPreferencesKey("photo_y_mm")
        private val KEY_W = floatPreferencesKey("photo_w_mm")
        private val KEY_H = floatPreferencesKey("photo_h_mm")
        private val KEY_FOOTER = stringPreferencesKey("default_footer")
        private val KEY_ROUNDING = stringPreferencesKey("default_rounding")
    }
}
