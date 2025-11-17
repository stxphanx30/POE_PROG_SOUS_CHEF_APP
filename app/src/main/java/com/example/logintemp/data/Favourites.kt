package com.example.logintemp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class Favourites(
    @PrimaryKey val mealId: String,
    val userId: String,
    val mealName: String,
    val mealThumb: String
)