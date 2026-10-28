package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
  INCOME,
  EXPENSE
}

enum class PaymentMethod(val displayName: String) {
  UPI("UPI (GPay/PhonePe)"),
  NET_BANKING("NetBanking"),
  CREDIT_CARD("Credit Card"),
  DEBIT_CARD("Debit Card"),
  CASH("Cash"),
  BANK_TRANSFER("Bank Transfer / IMPS")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: String = "user_default",
  val title: String,
  val amount: Double,
  val type: TransactionType,
  val category: String,
  val dateMillis: Long = System.currentTimeMillis(),
  val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
  val notes: String = ""
)
