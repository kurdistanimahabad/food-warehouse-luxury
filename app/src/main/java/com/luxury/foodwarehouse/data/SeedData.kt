package com.luxury.foodwarehouse.data

import com.luxury.foodwarehouse.model.FoodCategory
import java.util.concurrent.TimeUnit

object SeedData {
    fun sampleItems(now: Long = System.currentTimeMillis()): List<FoodItemEntity> {
        val day = TimeUnit.DAYS.toMillis(1)
        return listOf(
            FoodItemEntity(
                name = "کره حیوانی پاستوریزه ۱۰۰ گرمی",
                barcode = "6260111001",
                batchNumber = "B-409A",
                category = FoodCategory.DAIRY,
                unit = "کارتن",
                quantity = 24.0,
                minStockAlert = 8.0,
                purchasePrice = 1_850_000L,
                salePrice = 2_250_000L,
                storageLocation = "سردخانه صفر درجه",
                productionDateMillis = now - 20 * day,
                expiryDateMillis = now + 6 * day,
                supplierName = "لبنیات ممتاز شمال"
            ),
            FoodItemEntity(
                name = "روغن سرخ‌کردنی ویژه ۵ لیتری",
                barcode = "6260222004",
                batchNumber = "OIL-882",
                category = FoodCategory.CANNED_OIL,
                unit = "حلب",
                quantity = 45.0,
                minStockAlert = 10.0,
                purchasePrice = 1_420_000L,
                salePrice = 1_690_000L,
                storageLocation = "انبار خشک شماره ۱",
                productionDateMillis = now - 45 * day,
                expiryDateMillis = now + 240 * day,
                supplierName = "صنایع غذایی زرین"
            ),
            FoodItemEntity(
                name = "برنج طارم هاشمی درجه یک ۱۰ کیلویی",
                barcode = "6260333009",
                batchNumber = "RICE-102",
                category = FoodCategory.DRY_GOODS,
                unit = "کیسه",
                quantity = 38.0,
                minStockAlert = 12.0,
                purchasePrice = 3_600_000L,
                salePrice = 4_250_000L,
                storageLocation = "انبار خشک شماره ۲",
                productionDateMillis = now - 90 * day,
                expiryDateMillis = now + 275 * day,
                supplierName = "پخش غلات گیلان"
            ),
            FoodItemEntity(
                name = "آبمیوه طبیعی پرتقال ۱ لیتری",
                barcode = "6260444012",
                batchNumber = "J-771",
                category = FoodCategory.BEVERAGES,
                unit = "شیرینگ",
                quantity = 4.0,
                minStockAlert = 10.0,
                purchasePrice = 620_000L,
                salePrice = 790_000L,
                storageLocation = "انبار نیمه‌خنک",
                productionDateMillis = now - 30 * day,
                expiryDateMillis = now + 52 * day,
                supplierName = "نوشیدنی آفتاب"
            ),
            FoodItemEntity(
                name = "کنسرو ماهی تن در روغن زیتون",
                barcode = "6260555019",
                batchNumber = "TN-305",
                category = FoodCategory.CANNED_OIL,
                unit = "کارتن",
                quantity = 6.0,
                minStockAlert = 5.0,
                purchasePrice = 1_180_000L,
                salePrice = 1_450_000L,
                storageLocation = "انبار خشک شماره ۱",
                productionDateMillis = now - 400 * day,
                expiryDateMillis = now - 3 * day,
                supplierName = "شیلات جنوب"
            ),
            FoodItemEntity(
                name = "فیله مرغ منجمد بسته‌بندی",
                barcode = "6260666025",
                batchNumber = "CH-910",
                category = FoodCategory.PROTEIN,
                unit = "کارتن",
                quantity = 30.0,
                minStockAlert = 10.0,
                purchasePrice = 2_450_000L,
                salePrice = 2_890_000L,
                storageLocation = "تونل انجماد منفی ۱۸",
                productionDateMillis = now - 12 * day,
                expiryDateMillis = now + 11 * day,
                supplierName = "پروتئین سپید"
            )
        )
    }
}
