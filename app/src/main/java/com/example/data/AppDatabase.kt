package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfileEntity

@Database(
  entities = [
    TransactionEntity::class,
    BudgetEntity::class,
    UserProfileEntity::class
  ],
  version = 4,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun budgetDao(): BudgetDao
  abstract fun userProfileDao(): UserProfileDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "expense_tracker_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
