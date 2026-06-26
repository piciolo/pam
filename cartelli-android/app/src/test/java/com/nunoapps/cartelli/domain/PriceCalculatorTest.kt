package com.nunoapps.cartelli.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceCalculatorTest {

    @Test
    fun parseNum_handlesCommaAndThousands() {
        assertEquals(1.19, PriceCalculator.parseNum("1,19")!!, 1e-9)
        assertEquals(1234.56, PriceCalculator.parseNum("1.234,56")!!, 1e-9)
        assertNull(PriceCalculator.parseNum(""))
        assertNull(PriceCalculator.parseNum("abc"))
    }

    @Test
    fun fmt_twoDecimalsWithComma() {
        assertEquals("1,80", PriceCalculator.fmt(1.803))
        assertEquals("2,00", PriceCalculator.fmt(2.0))
    }

    /** Caso del cartello attuale: birra 66 cl a 1,19 € -> al lt 1,81 (ceil PAM). */
    @Test
    fun unitPrice_liquid_ceil() {
        val c = PriceCalculator.compute(
            CartelloState(prezzo = "1,19", qty = "66", unit = ProductUnit.CL, rounding = Rounding.CEIL)
        )
        assertEquals("cl 66 - al lt  € 1,81", c.unitLine)
        assertEquals("1", c.priceInt)
        assertEquals("19", c.priceDec)
    }

    @Test
    fun unitPrice_liquid_round() {
        val c = PriceCalculator.compute(
            CartelloState(prezzo = "1,19", qty = "66", unit = ProductUnit.CL, rounding = Rounding.ROUND)
        )
        // 1.19 / 0.66 = 1.8030... -> round -> 1,80
        assertEquals("cl 66 - al lt  € 1,80", c.unitLine)
    }

    @Test
    fun unitPrice_solid_kg() {
        // 250 g a 3,49 -> /0.25 = 13.96 €/kg
        val c = PriceCalculator.compute(
            CartelloState(prezzo = "3,49", qty = "250", unit = ProductUnit.G, rounding = Rounding.CEIL)
        )
        assertEquals("g 250 - al kg  € 13,96", c.unitLine)
    }

    @Test
    fun piece_hasNoUnitLine() {
        val c = PriceCalculator.compute(
            CartelloState(prezzo = "1,19", qty = "1", unit = ProductUnit.PZ)
        )
        assertNull(c.unitLine)
    }

    @Test
    fun discountBox_percent() {
        val c = PriceCalculator.compute(
            CartelloState(prezzo = "1,19", qty = "66", unit = ProductUnit.CL, prezzoBase = "1,59")
        )
        // (1.59-1.19)/1.59*100 = 25.15 -> 25
        assertEquals(25, c.scontoPerc)
    }

    @Test
    fun noDiscount_whenBaseNotGreater() {
        val c = PriceCalculator.compute(
            CartelloState(prezzo = "1,19", prezzoBase = "1,00")
        )
        assertNull(c.scontoPerc)
    }

    @Test
    fun emptyPrice_isInvalid() {
        val c = PriceCalculator.compute(CartelloState())
        assertTrue(!c.hasPrice)
    }
}
