package com.nunoapps.cartelli.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.nunoapps.cartelli.R

/** Font e immagini necessari al rendering del cartello, caricati una volta sola. */
class CartelloAssets(
    val pamPrice: Typeface,
    val pamText: Typeface,
    val bannerOfferta: Bitmap,
    val bannerConviene: Bitmap,
    val logo: Bitmap,
) {
    companion object {
        fun load(context: Context): CartelloAssets {
            val price = ResourcesCompat.getFont(context, R.font.pam_price) ?: Typeface.DEFAULT_BOLD
            val text = ResourcesCompat.getFont(context, R.font.pam_text) ?: Typeface.DEFAULT_BOLD
            fun bmp(id: Int) = BitmapFactory.decodeResource(context.resources, id)
            return CartelloAssets(
                pamPrice = price,
                pamText = text,
                bannerOfferta = bmp(R.drawable.banner_offerta),
                bannerConviene = bmp(R.drawable.banner_conviene),
                logo = bmp(R.drawable.logo_pam),
            )
        }
    }
}
