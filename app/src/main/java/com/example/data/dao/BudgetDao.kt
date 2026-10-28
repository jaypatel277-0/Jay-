package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
  @Query("SELECT * FROM budgets WHERE userId = :userId AND periodMonth = :month AND periodYear = :year")
  fun getBudgetsForMonth(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>>

  @Query("SELECT * FROM budgets WHERE userId = :userId")
  fun getAllBudgets(userId: String): Flow<List<BudgetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudget(budget: BudgetEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(budgets: List<BudgetEntity>)

  @Update
  suspend fun updateBudget(budget: BudgetEntity)

  @Delete
  suspend fun deleteBudget(budget: BudgetEntity)

  @Query("DELETE FROM budgets WHERE id = :id")
  suspend fun deleteBudgetById(id: Long)

  @Query("DELETE FROM budgets WHERE userId = :userId")
  suspend fun deleteAll(userId: String)

  @Query("SELECT COUNT(*) FROM budgets WHERE userId = :userId")
  suspend fun getCount(userId: String): Int
}
