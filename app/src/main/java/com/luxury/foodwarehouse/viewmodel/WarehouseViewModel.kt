package com.luxury.foodwarehouse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luxury.foodwarehouse.BuildConfig
import com.luxury.foodwarehouse.data.CategoryEntity
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.data.SeedData
import com.luxury.foodwarehouse.data.StockMovementEntity
import com.luxury.foodwarehouse.data.WarehouseDatabase
import com.luxury.foodwarehouse.export.WarehouseReportExporter
import com.luxury.foodwarehouse.model.ExpiryStatus
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

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _selectedFilter = MutableStateFlow(InventoryFilter.ALL)
    val selectedFilter: StateFlow<InventoryFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateUiState())
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = dao.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        _selectedCategoryId,
        _selectedFilter,
        _searchQuery
    ) { items, categoryId, filter, query ->
        items.filter { item ->
            val matchesCategory = categoryId == null || item.categoryId == categoryId
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
            if (dao.getCategoryCount() == 0) {
                dao.insertAllCategories(SeedData.defaultCategories())
            }
            if (dao.getItemCount() == 0) {
                dao.insertAllItems(SeedData.sampleItems())
                SeedData.sampleMovements().forEach { dao.insertMovement(it) }
            }
        }
    }

    fun setCategoryId(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setFilter(filter: InventoryFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val idx = categories.value.size
            dao.insertCategory(CategoryEntity(name = trimmed, colorIndex = idx))
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            dao.updateCategory(category.copy(name = trimmed))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val currentCats = categories.value
            if (currentCats.size <= 1) return@launch
            val fallback = currentCats.firstOrNull { it.id != category.id } ?: return@launch
            dao.reassignItemsCategory(oldCategoryId = category.id, newCategoryId = fallback.id)
            dao.deleteCategory(category)
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = null
            }
        }
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
            applySingleMovement(item, type, qtyDelta, note)
        }
    }

    fun applyBulkMovements(
        entries: List<Pair<FoodItemEntity, Double>>,
        type: MovementType,
        note: String = ""
    ) {
        if (entries.isEmpty()) return
        viewModelScope.launch {
            entries.forEach { (item, qtyDelta) ->
                if (qtyDelta > 0.0) {
                    applySingleMovement(
                        item = item,
                        type = type,
                        qtyDelta = qtyDelta,
                        note = note.ifBlank { "عملیات گروهی (${type.titleFa})" }
                    )
                }
            }
        }
    }

    private suspend fun applySingleMovement(
        item: FoodItemEntity,
        type: MovementType,
        qtyDelta: Double,
        note: String
    ) {
        val newQty = when (type) {
            MovementType.INBOUND -> item.quantity + qtyDelta
            MovementType.OUTBOUND, MovementType.WASTE -> (item.quantity - qtyDelta).coerceAtLeast(0.0)
        }
        val unitPrice = if (type == MovementType.OUTBOUND) item.salePrice else item.purchasePrice
        val cartonFactor = if (item.unit.trim() == "کارتن") item.unitsPerCarton.coerceAtLeast(1).toLong() else 1L
        val amount = (qtyDelta * unitPrice * cartonFactor).toLong()

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

    fun exportCsvReport() {
        exporter.exportCsvAndShare(allItems.value, categories.value)
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
