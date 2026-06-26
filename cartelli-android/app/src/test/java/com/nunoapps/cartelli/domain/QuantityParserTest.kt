package com.nunoapps.cartelli.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuantityParserTest {

    @Test
    fun single_grams() {
        val p = QuantityParser.parse("250 g")!!
        assertEquals("250", p.qty)
        assertEquals(ProductUnit.G, p.unit)
    }

    @Test
    fun single_litre_decimal() {
        val p = QuantityParser.parse("1,5 L")!!
        assertEquals("1,5", p.qty)
        assertEquals(ProductUnit.L, p.unit)
    }

    @Test
    fun alias_gr_and_lt() {
        assertEquals(ProductUnit.G, QuantityParser.parse("500 gr")!!.unit)
        assertEquals(ProductUnit.L, QuantityParser.parse("1 lt")!!.unit)
    }

    @Test
    fun pack_multiplies() {
        // 6 x 33 cl -> 198 cl
        val p = QuantityParser.parse("6 x 33 cl")!!
        assertEquals("198", p.qty)
        assertEquals(ProductUnit.CL, p.unit)
    }

    @Test
    fun unparseable_returnsNull() {
        assertNull(QuantityParser.parse("una confezione"))
        assertNull(QuantityParser.parse(""))
        assertNull(QuantityParser.parse(null))
    }
}
