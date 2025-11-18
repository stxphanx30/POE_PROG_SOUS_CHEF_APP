package com.example.logintemp.data.recipe

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    val name: String,
    val category: String,

    @ColumnInfo(name = "cook_time_minutes")
    val cookTimeMinutes: Int? = null,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()

)
