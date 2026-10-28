package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  fun getUserProfile(userId: String): Flow<UserProfileEntity?>

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserProfileOnce(userId: String): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserProfileEntity)

  @Update
  suspend fun updateUser(user: UserProfileEntity)
}
