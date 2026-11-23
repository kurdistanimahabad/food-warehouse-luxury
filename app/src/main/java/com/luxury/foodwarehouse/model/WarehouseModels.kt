package com.luxury.foodwarehouse.model

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ExpiryStatus(val labelFa: String) {
    EXPIRED("منقضی‌شده"),
    EXPIRING_SOON("نزدیک انقضا"),
    HEALTHY("سالم")
}

enum class MovementType(val titleFa: String) {
    INBOUND("ورود به انبار"),
    OUTBOUND("خروج / فروش"),
    WASTE("ضایعات / مرجوعی")
}

data class GitHubReleaseInfo(
    val tagName: String,
    val title: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?,
    val publishedAt: String
)

data class WarehouseSummary(
    val totalPurchaseValue: Long = 0L,
    val totalSaleValue: Long = 0L,
    val estimatedProfit: Long = 0L,
    val totalQuantityUnits: Double = 0.0,
    val totalSkuCount: Int = 0,
    val expiredCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalWasteValue: Long = 0L,
    val totalOutboundRevenue: Long = 0L
)

object Formatters {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    fun formatCurrency(amount: Long, unitLabel: String = "تومان"): String {
        return "${numberFormat.format(amount)} $unitLabel"
    }

    fun formatQty(qty: Double, unit: String = ""): String {
        val clean = if (qty % 1.0 == 0.0) qty.toLong().toString() else String.format(Locale.US, "%.1f", qty)
        return if (unit.isBlank()) clean else "$clean $unit"
    }

    fun formatDate(timestampMillis: Long, includeMonthName: Boolean = false): String {
        return JalaliCalendar.formatJalali(timestampMillis, includeMonthName)
    }

    fun daysUntil(expiryMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long {
        val diff = expiryMillis - nowMillis
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun getExpiryStatus(expiryMillis: Long, warningDays: Int = 14): ExpiryStatus {
        val days = daysUntil(expiryMillis)
        return when {
            days < 0 -> ExpiryStatus.EXPIRED
            days <= warningDays -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.HEALTHY
        }
    }
}
