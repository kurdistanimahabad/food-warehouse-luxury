package com.luxury.foodwarehouse.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.luxury.foodwarehouse.data.CategoryEntity
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.data.StockMovementEntity
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.WarehouseSummary
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WarehouseReportExporter(private val context: Context) {

    fun exportCsvAndShare(items: List<FoodItemEntity>, categories: List<CategoryEntity> = emptyList()): File {
        val categoryMap = categories.associateBy { it.id }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val outDir = File(context.getExternalFilesDir(null), "reports").apply { mkdirs() }
        val csvFile = File(outDir, "inventory_report_$timestamp.csv")

        val header = listOf(
            "شناسه",
            "نام محصول",
            "دسته‌بندی",
            "بارکد",
            "سری ساخت",
            "محل نگهداری",
            "موجودی",
            "واحد",
            "قیمت خرید",
            "قیمت فروش",
            "ارزش کل خرید",
            "ارزش کل فروش",
            "تاریخ تولید (شمسی)",
            "تاریخ انقضا (شمسی)",
            "وضعیت انقضا"
        ).joinToString(",") { escapeCsv(it) }

        val rows = items.map { item ->
            val status = Formatters.getExpiryStatus(item.expiryDateMillis).labelFa
            val catName = categoryMap[item.categoryId]?.name ?: "سایر اقلام"
            listOf(
                item.id.toString(),
                item.name,
                catName,
                item.barcode,
                item.batchNumber,
                item.storageLocation,
                item.quantity.toString(),
                item.unit,
                item.purchasePrice.toString(),
                item.salePrice.toString(),
                item.totalPurchaseValue.toString(),
                item.totalSaleValue.toString(),
                Formatters.formatDate(item.productionDateMillis),
                Formatters.formatDate(item.expiryDateMillis),
                status
            ).joinToString(",") { escapeCsv(it) }
        }

        val content = buildString {
            append('\uFEFF')
            appendLine(header)
            rows.forEach { appendLine(it) }
        }

        csvFile.writeText(content, Charsets.UTF_8)
        shareFile(csvFile, "text/csv")
        return csvFile
    }

    fun buildSummaryReportText(summary: WarehouseSummary): String {
        return buildString {
            appendLine("گزارش جامع وضعیت انبار مواد غذایی")
            appendLine("--------------------------------")
            appendLine("تعداد اقلام (SKU): ${summary.totalSkuCount}")
            appendLine("ارزش کل موجودی (خرید): ${Formatters.formatCurrency(summary.totalPurchaseValue)}")
            appendLine("ارزش فروش بالقوه: ${Formatters.formatCurrency(summary.totalSaleValue)}")
            appendLine("سود ناخالص تخمینی: ${Formatters.formatCurrency(summary.estimatedProfit)}")
            appendLine("اقلام نزدیک انقضا: ${summary.expiringSoonCount}")
            appendLine("اقلام منقضی‌شده: ${summary.expiredCount}")
            appendLine("اقلام کم‌موجود: ${summary.lowStockCount}")
            appendLine("ارزش ضایعات ثبت‌شده: ${Formatters.formatCurrency(summary.totalWasteValue)}")
        }
    }

    fun exportJsonBackup(items: List<FoodItemEntity>, movements: List<StockMovementEntity>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val outDir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val backupFile = File(outDir, "warehouse_backup_$timestamp.json")

        val itemsArray = JSONArray()
        items.forEach { item ->
            itemsArray.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("barcode", item.barcode)
                    put("batchNumber", item.batchNumber)
                    put("categoryId", item.categoryId)
                    put("unit", item.unit)
                    put("quantity", item.quantity)
                    put("minStockAlert", item.minStockAlert)
                    put("purchasePrice", item.purchasePrice)
                    put("salePrice", item.salePrice)
                    put("storageLocation", item.storageLocation)
                    put("productionDateMillis", item.productionDateMillis)
                    put("expiryDateMillis", item.expiryDateMillis)
                    put("supplierName", item.supplierName)
                }
            )
        }

        val movementsArray = JSONArray()
        movements.forEach { m ->
            movementsArray.put(
                JSONObject().apply {
                    put("itemId", m.itemId)
                    put("itemName", m.itemName)
                    put("movementType", m.movementType.name)
                    put("quantity", m.quantity)
                    put("unitPrice", m.unitPrice)
                    put("totalAmount", m.totalAmount)
                    put("note", m.note)
                    put("timestampMillis", m.timestampMillis)
                }
            )
        }

        val root = JSONObject().apply {
            put("exportedAt", System.currentTimeMillis())
            put("items", itemsArray)
            put("movements", movementsArray)
        }

        backupFile.writeText(root.toString(2), Charsets.UTF_8)
        shareFile(backupFile, "application/json")
        return backupFile
    }

    fun shareTextReport(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "اشتراک‌گذاری گزارش انبار").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "ارسال فایل گزارش").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun escapeCsv(raw: String): String {
        val cleaned = raw.replace("\"", "\"\"")
        return "\"$cleaned\""
    }
}
