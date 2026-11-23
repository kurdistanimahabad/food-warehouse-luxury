package com.luxury.foodwarehouse.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxury.foodwarehouse.model.FoodCategory

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val barcode: String = "",
    val batchNumber: String = "",
    val category: FoodCategory,
    val unit: String = "کارتن",
    val quantity: Double,
    val minStockAlert: Double = 5.0,
    val purchasePrice: Long,
    val salePrice: Long,
    val storageLocation: String = "انبار مرکزی",
    val productionDateMillis: Long,
    val expiryDateMillis: Long,
    val supplierName: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val totalPurchaseValue: Long
        get() = (quantity * purchasePrice).toLong()

    val totalSaleValue: Long
        get() = (quantity * salePrice).toLong()

    val isLowStock: Boolean
        get() = quantity <= minStockAlert
}
