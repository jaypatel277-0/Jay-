package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: String = "user_default",
  val category: String, // e.g. "Food & Dining", "Rent & Housing", or "TOTAL_BUDGET"
  val limitAmount: Double,
  val periodMonth: Int, // 1 to 12
  val periodYear: Int   // e.g. 2026
)
