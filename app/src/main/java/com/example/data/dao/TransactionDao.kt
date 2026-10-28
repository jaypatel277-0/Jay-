package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateMillis DESC")
  fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE userId = :userId AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
  fun getTransactionsInRange(userId: String, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Delete
  suspend fun deleteTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Query("DELETE FROM transactions WHERE userId = :userId")
  suspend fun deleteAll(userId: String)

  @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId")
  suspend fun getCount(userId: String): Int
}
