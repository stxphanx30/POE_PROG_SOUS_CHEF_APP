package com.example.logintemp.data.notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(row: NotificationEntity): Long

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)
}