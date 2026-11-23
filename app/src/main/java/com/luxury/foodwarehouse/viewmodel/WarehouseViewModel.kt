package com.luxury.foodwarehouse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luxury.foodwarehouse.BuildConfig
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.data.SeedData
import com.luxury.foodwarehouse.data.StockMovementEntity
import com.luxury.foodwarehouse.data.WarehouseDatabase
import com.luxury.foodwarehouse.export.WarehouseReportExporter
import com.luxury.foodwarehouse.model.ExpiryStatus
import com.luxury.foodwarehouse.model.FoodCategory
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.GitHubReleaseInfo
import com.luxury.foodwarehouse.model.MovementType
import com.luxury.foodwarehouse.model.WarehouseSummary
import com.luxury.foodwarehouse.update.AppUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class InventoryFilter {
    ALL, EXPIRING_SOON, EXPIRED, LOW_STOCK
}

data class UpdateUiState(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val latestRelease: GitHubReleaseInfo? = null,
    val statusMessage: String? = null,
    val downloadEnqueued: Boolean = false
)

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = WarehouseDatabase.getDatabase(application).warehouseDao()
    private val updateManager = AppUpdateManager(application)
    private val exporter = WarehouseReportExporter(application)

    private val _selectedCategory = MutableStateFlow<FoodCategory?>(null)
    val selectedCategory: StateFlow<FoodCategory?> = _selectedCategory.asStateFlow()

    private val _selectedFilter = MutableStateFlow(InventoryFilter.ALL)
    val selectedFilter: StateFlow<InventoryFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateUiState())
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    val allItems: StateFlow<List<FoodItemEntity>> = dao.observeAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movements: StateFlow<List<StockMovementEntity>> = dao.observeRecentMovements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<WarehouseSummary> = combine(allItems, movements) { items, movs ->
        val totalPurchase = items.sumOf { it.totalPurchaseValue }
        val totalSale = items.sumOf { it.totalSaleValue }
        val expired = items.count { Formatters.getExpiryStatus(it.expiryDateMillis) == ExpiryStatus.EXPIRED }
        val expiringSoon = items.count { Formatters.getExpiryStatus(it.expiryDateMillis) == ExpiryStatus.EXPIRING_SOON }
        val lowStock = items.count { it.isLowStock }
        val wasteValue = movs.filter { it.movementType == MovementType.WASTE }.sumOf { it.totalAmount }
        val outboundRevenue = movs.filter { it.movementType == MovementType.OUTBOUND }.sumOf { it.totalAmount }

        WarehouseSummary(
            totalPurchaseValue = totalPurchase,
            totalSaleValue = totalSale,
            estimatedProfit = totalSale - totalPurchase,
            totalQuantityUnits = items.sumOf { it.quantity },
            totalSkuCount = items.size,
            expiredCount = expired,
            expiringSoonCount = expiringSoon,
            lowStockCount = lowStock,
            totalWasteValue = wasteValue,
            totalOutboundRevenue = outboundRevenue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WarehouseSummary())

    val filteredItems: StateFlow<List<FoodItemEntity>> = combine(
        allItems,
        _selectedCategory,
        _selectedFilter,
        _searchQuery
    ) { items, category, filter, query ->
        items.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.barcode.contains(query, ignoreCase = true) ||
                item.batchNumber.contains(query, ignoreCase = true) ||
                item.storageLocation.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                InventoryFilter.ALL -> true
                InventoryFilter.EXPIRING_SOON ->
                    Formatters.getExpiryStatus(item.expiryDateMillis) == ExpiryStatus.EXPIRING_SOON
                InventoryFilter.EXPIRED ->
                    Formatters.getExpiryStatus(item.expiryDateMillis) == ExpiryStatus.EXPIRED
                InventoryFilter.LOW_STOCK -> item.isLowStock
            }
            matchesCategory && matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (dao.getItemCount() == 0) {
                dao.insertAllItems(SeedData.sampleItems())
            }
        }
    }

    fun setCategory(category: FoodCategory?) {
        _selectedCategory.value = category
    }

    fun setFilter(filter: InventoryFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addOrUpdateItem(item: FoodItemEntity, isNew: Boolean) {
        viewModelScope.launch {
            val updated = item.copy(updatedAtMillis = System.currentTimeMillis())
            if (isNew) {
                val id = dao.insertItem(updated)
                dao.insertMovement(
                    StockMovementEntity(
                        itemId = id,
                        itemName = updated.name,
                        movementType = MovementType.INBOUND,
                        quantity = updated.quantity,
                        unitPrice = updated.purchasePrice,
                        totalAmount = updated.totalPurchaseValue,
                        note = "ثبت اولیه محصول جدید"
                    )
                )
            } else {
                dao.updateItem(updated)
            }
        }
    }

    fun deleteItem(item: FoodItemEntity) {
        viewModelScope.launch {
            dao.deleteItem(item)
        }
    }

    fun recordMovement(
        item: FoodItemEntity,
        type: MovementType,
        qtyDelta: Double,
        note: String = ""
    ) {
        if (qtyDelta <= 0.0) return
        viewModelScope.launch {
            val newQty = when (type) {
                MovementType.INBOUND -> item.quantity + qtyDelta
                MovementType.OUTBOUND, MovementType.WASTE -> (item.quantity - qtyDelta).coerceAtLeast(0.0)
            }
            val unitPrice = if (type == MovementType.OUTBOUND) item.salePrice else item.purchasePrice
            val amount = (qtyDelta * unitPrice).toLong()

            dao.updateItem(item.copy(quantity = newQty, updatedAtMillis = System.currentTimeMillis()))
            dao.insertMovement(
                StockMovementEntity(
                    itemId = item.id,
                    itemName = item.name,
                    movementType = type,
                    quantity = qtyDelta,
                    unitPrice = unitPrice,
                    totalAmount = amount,
                    note = note.ifBlank { type.titleFa }
                )
            )
        }
    }

    fun exportCsvReport() {
        exporter.exportCsvAndShare(allItems.value)
    }

    fun shareTextSummary() {
        exporter.shareTextReport(exporter.buildSummaryReportText(summary.value))
    }

    fun exportJsonBackup() {
        exporter.exportJsonBackup(allItems.value, movements.value)
    }

    fun checkForUpdates(githubOwner: String, githubRepo: String) {
        viewModelScope.launch {
            _updateState.value = UpdateUiState(isChecking = true)
            val result = updateManager.fetchLatestRelease(githubOwner.trim(), githubRepo.trim())
            result.onSuccess { release ->
                val hasUpdate = updateManager.isNewerVersion(BuildConfig.VERSION_NAME, release.tagName)
                _updateState.value = UpdateUiState(
                    isChecking = false,
                    updateAvailable = hasUpdate,
                    latestRelease = release,
                    statusMessage = if (hasUpdate) {
                        "نسخه جدید ${release.tagName} آماده دانلود است."
                    } else {
                        "شما از آخرین نسخه برنامه (${BuildConfig.VERSION_NAME}) استفاده می‌کنید."
                    }
                )
            }.onFailure { err ->
                _updateState.value = UpdateUiState(
                    isChecking = false,
                    updateAvailable = false,
                    statusMessage = err.message ?: "خطا در ارتباط با سرور بروزرسانی"
                )
            }
        }
    }

    fun downloadUpdateApk(apkUrl: String) {
        updateManager.enqueueApkDownload(apkUrl)
        _updateState.value = _updateState.value.copy(
            downloadEnqueued = true,
            statusMessage = "دانلود فایل بروزرسانی آغاز شد. پس از اتمام از دکمه نصب اقدام کنید."
        )
    }

    fun triggerInstallApk() {
        updateManager.installApkFromDownloads()
    }
}
