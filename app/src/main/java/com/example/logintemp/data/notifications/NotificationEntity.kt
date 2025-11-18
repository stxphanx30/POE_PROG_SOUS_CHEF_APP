package com.example.logintemp.data.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val relatedMealPlanId: Long? = null
)