package com.luxury.foodwarehouse.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxury.foodwarehouse.model.MovementType

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val itemId: Long,
    val itemName: String,
    val movementType: MovementType,
    val quantity: Double,
    val unitPrice: Long,
    val totalAmount: Long,
    val note: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)
