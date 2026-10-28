package com.example.data

import androidx.room.TypeConverter
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType

class Converters {
  @TypeConverter
  fun fromTransactionType(type: TransactionType): String = type.name

  @TypeConverter
  fun toTransactionType(value: String): TransactionType = try {
    TransactionType.valueOf(value)
  } catch (e: Exception) {
    TransactionType.EXPENSE
  }

  @TypeConverter
  fun fromPaymentMethod(method: PaymentMethod): String = method.name

  @TypeConverter
  fun toPaymentMethod(value: String): PaymentMethod = try {
    PaymentMethod.valueOf(value)
  } catch (e: Exception) {
    PaymentMethod.CASH
  }
}
