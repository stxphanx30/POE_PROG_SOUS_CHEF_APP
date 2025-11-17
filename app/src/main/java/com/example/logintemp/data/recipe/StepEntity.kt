package com.example.logintemp.data.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recipeId: Long,
    val text: String,
    val order: Int = 0
)