package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryEntertainment
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryFreelance
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryHousing
import com.example.ui.theme.CategoryInvestment
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategorySalary
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryTransport
import com.example.ui.theme.CategoryUtilities

data class CategoryMeta(
  val name: String,
  val icon: ImageVector,
  val color: Color,
  val isIncome: Boolean = false,
  val subtitle: String = ""
)

object CategoryConstants {
  val EXPENSE_CATEGORIES = listOf(
    CategoryMeta("Groceries / Ration", Icons.Default.ShoppingCart, Color(0xFF16A34A), subtitle = "Blinkit, Zepto, DMart, Kirana"),
    CategoryMeta("Rent / Maintenance", Icons.Default.Home, Color(0xFF4F46E5), subtitle = "Flat rent & society maintenance"),
    CategoryMeta("Utilities (Power/Gas/Water)", Icons.Default.Power, Color(0xFF0D9488), subtitle = "Electricity, PNG/LPG, Water"),
    CategoryMeta("Mobile / Broadband", Icons.Default.PhoneAndroid, Color(0xFF0284C7), subtitle = "Jio, Airtel, Fiber wifi"),
    CategoryMeta("Maid / Cook Salary", Icons.Default.CleaningServices, Color(0xFFD97706), subtitle = "Cook, maid, domestic help"),
    CategoryMeta("Fuel / Cab (Ola/Uber)", Icons.Default.LocalGasStation, Color(0xFFEA580C), subtitle = "Petrol, Diesel, Uber, Ola"),
    CategoryMeta("Swiggy / Zomato", Icons.Default.Fastfood, Color(0xFFE11D48), subtitle = "Online food delivery & dining"),
    CategoryMeta("SIP / Mutual Funds", Icons.Default.TrendingUp, Color(0xFF059669), subtitle = "Zerodha, Groww monthly SIPs"),
    CategoryMeta("Insurance (LIC/Health)", Icons.Default.Security, Color(0xFF7C3AED), subtitle = "Term life, LIC, health cover"),
    CategoryMeta("Shopping", Icons.Default.ShoppingBag, Color(0xFFDB2777), subtitle = "Amazon, Flipkart, Myntra"),
    CategoryMeta("OTT Subscriptions", Icons.Default.Tv, Color(0xFF9333EA), subtitle = "Netflix, Prime, Hotstar"),
    CategoryMeta("Other Expense", Icons.Default.Category, Color(0xFF64748B), subtitle = "Miscellaneous expenses")
  )

  val INCOME_CATEGORIES = listOf(
    CategoryMeta("Salary", Icons.Default.Payments, Color(0xFF059669), isIncome = true, subtitle = "Monthly company paycheck"),
    CategoryMeta("Freelance / Gig", Icons.Default.Work, Color(0xFF2563EB), isIncome = true, subtitle = "Consulting, freelance gigs"),
    CategoryMeta("Dividend", Icons.Default.TrendingUp, Color(0xFF0891B2), isIncome = true, subtitle = "Stock dividends & payouts"),
    CategoryMeta("Fixed Deposit Interest", Icons.Default.AccountBalance, Color(0xFF7C3AED), isIncome = true, subtitle = "Bank FD, RD interest"),
    CategoryMeta("Rental Income", Icons.Default.Home, Color(0xFF4F46E5), isIncome = true, subtitle = "Property / flat rent received"),
    CategoryMeta("Cashback / Rewards", Icons.Default.Redeem, Color(0xFFD97706), isIncome = true, subtitle = "Cred, GPay, Paytm cashback"),
    CategoryMeta("Other Income", Icons.Default.CurrencyRupee, Color(0xFF64748B), isIncome = true, subtitle = "Gifts & miscellaneous income")
  )

  fun getCategoryMeta(name: String): CategoryMeta {
    return (EXPENSE_CATEGORIES + INCOME_CATEGORIES).firstOrNull {
      it.name.equals(name, ignoreCase = true) || it.name.startsWith(name, ignoreCase = true)
    } ?: CategoryMeta(name, Icons.Default.Category, Color(0xFF64748B))
  }
}
