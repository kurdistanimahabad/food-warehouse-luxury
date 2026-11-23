package com.luxury.foodwarehouse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val barcode: String = "",
    val batchNumber: String = "",
    val categoryId: Long = 1L,
    val unit: String = "کارتن",
    val unitsPerCarton: Int = 1,
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
    private val effectiveCartonMultiplier: Long
        get() = if (unit.trim() == "کارتن") unitsPerCarton.coerceAtLeast(1).toLong() else 1L

    val cartonPurchasePrice: Long
        get() = purchasePrice * unitsPerCarton.coerceAtLeast(1)

    val cartonSalePrice: Long
        get() = salePrice * unitsPerCarton.coerceAtLeast(1)

    val totalPurchaseValue: Long
        get() = (quantity * purchasePrice * effectiveCartonMultiplier).toLong()

    val totalSaleValue: Long
        get() = (quantity * salePrice * effectiveCartonMultiplier).toLong()

    val isLowStock: Boolean
        get() = quantity <= minStockAlert
}
