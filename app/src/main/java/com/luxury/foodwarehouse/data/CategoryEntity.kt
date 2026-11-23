package com.luxury.foodwarehouse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val colorIndex: Int = 0,
    val isDefault: Boolean = false
)
