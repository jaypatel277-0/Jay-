package com.example.ui.components

import java.util.Locale
import kotlin.math.abs

object IndianCurrencyFormatter {

  /**
   * Formats a double value according to the Indian numbering system.
   * Example:
   * 150000.0 -> "₹1,50,000.00"
   * 1245600.50 -> "₹12,45,600.50"
   * 10000000.0 -> "₹1,00,00,000.00"
   * -5200.0 -> "-₹5,200.00"
   */
  fun formatInr(
    amount: Double,
    includeSymbol: Boolean = true,
    currencySymbol: String = "₹",
    includeDecimals: Boolean = true
  ): String {
    val isNegative = amount < 0
    val absAmount = abs(amount)

    val longPart = absAmount.toLong()
    val decimalPart = String.format(Locale.US, "%.2f", absAmount - longPart).removePrefix("0")

    val strLong = longPart.toString()
    val formattedLong = if (strLong.length <= 3) {
      strLong
    } else {
      val lastThree = strLong.takeLast(3)
      val remaining = strLong.dropLast(3)

      val sb = StringBuilder()
      var count = 0
      for (i in remaining.length - 1 downTo 0) {
        sb.append(remaining[i])
        count++
        if (count == 2 && i != 0) {
          sb.append(',')
          count = 0
        }
      }
      val reversedRemaining = sb.reverse().toString()
      "$reversedRemaining,$lastThree"
    }

    val numberString = if (includeDecimals) {
      "$formattedLong$decimalPart"
    } else {
      formattedLong
    }

    val symbolPrefix = if (includeSymbol) currencySymbol else ""
    return if (isNegative) "-$symbolPrefix$numberString" else "$symbolPrefix$numberString"
  }

  /**
   * Compact format in Lakhs (L) and Crores (Cr) for chart labels or tight spaces
   */
  fun formatInrCompact(amount: Double, currencySymbol: String = "₹"): String {
    val absAmount = abs(amount)
    val prefix = if (amount < 0) "-$currencySymbol" else currencySymbol
    return when {
      absAmount >= 10_000_000 -> "$prefix${String.format(Locale.US, "%.2f", absAmount / 10_000_000)} Cr"
      absAmount >= 100_000 -> "$prefix${String.format(Locale.US, "%.2f", absAmount / 100_000)} L"
      absAmount >= 1_000 -> "$prefix${String.format(Locale.US, "%.1f", absAmount / 1_000)}k"
      else -> "$prefix${String.format(Locale.US, "%.0f", absAmount)}"
    }
  }
}
