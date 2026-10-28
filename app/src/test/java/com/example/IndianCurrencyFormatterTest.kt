package com.example

import com.example.ui.components.IndianCurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class IndianCurrencyFormatterTest {

  @Test
  fun testIndianNumberFormattingStandard() {
    // 1 Lakh 50 Thousand
    val formatted1 = IndianCurrencyFormatter.formatInr(150000.0, includeSymbol = true, currencySymbol = "₹")
    assertEquals("₹1,50,000.00", formatted1)

    // 12 Lakhs 45 Thousand 600
    val formatted2 = IndianCurrencyFormatter.formatInr(1245600.50, includeSymbol = true, currencySymbol = "₹")
    assertEquals("₹12,45,600.50", formatted2)

    // 1 Crore
    val formatted3 = IndianCurrencyFormatter.formatInr(10000000.0, includeSymbol = true, currencySymbol = "₹")
    assertEquals("₹1,00,00,000.00", formatted3)

    // Under 1,000
    val formatted4 = IndianCurrencyFormatter.formatInr(850.75, includeSymbol = true, currencySymbol = "₹")
    assertEquals("₹850.75", formatted4)

    // Negative number
    val formatted5 = IndianCurrencyFormatter.formatInr(-5200.0, includeSymbol = true, currencySymbol = "₹")
    assertEquals("-₹5,200.00", formatted5)
  }

  @Test
  fun testIndianNumberFormattingCompact() {
    val compactCrore = IndianCurrencyFormatter.formatInrCompact(12500000.0, currencySymbol = "₹")
    assertEquals("₹1.25 Cr", compactCrore)

    val compactLakh = IndianCurrencyFormatter.formatInrCompact(350000.0, currencySymbol = "₹")
    assertEquals("₹3.50 L", compactLakh)
  }
}
