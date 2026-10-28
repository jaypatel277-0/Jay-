package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserProfileEntity(
  @PrimaryKey
  val id: String = "user_default",
  val username: String = "Aarav Sharma",
  val email: String = "aarav.sharma@example.in",
  val pinHash: String = "", // Hashed 4-digit PIN or empty if not set
  val isPinLockEnabled: Boolean = false,
  val isBiometricEnabled: Boolean = true,
  val currencySymbol: String = "₹",
  val monthlyIncomeTarget: Double = 85000.0,
  val monthlySavingsGoal: Double = 25000.0,
  val isDarkModeEnabled: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)
