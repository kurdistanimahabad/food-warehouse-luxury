package com.luxury.foodwarehouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.ui.components.AddEditItemDialog
import com.luxury.foodwarehouse.ui.components.BulkMovementDialog
import com.luxury.foodwarehouse.ui.components.CategoryManagementDialog
import com.luxury.foodwarehouse.ui.screens.AnalyticsScreen
import com.luxury.foodwarehouse.ui.screens.DashboardScreen
import com.luxury.foodwarehouse.ui.screens.InventoryScreen
import com.luxury.foodwarehouse.ui.screens.SettingsUpdateScreen
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.FoodWarehouseLuxuryTheme
import com.luxury.foodwarehouse.ui.theme.LuxurySurface
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury
import com.luxury.foodwarehouse.viewmodel.WarehouseViewModel

private enum class AppTab(val title: String, val icon: ImageVector) {
    DASHBOARD("داشبورد", Icons.Rounded.Dashboard),
    INVENTORY("موجودی", Icons.Rounded.Inventory),
    ANALYTICS("نمودارها", Icons.Rounded.Analytics),
    UPDATE("آپدیت", Icons.Rounded.SystemUpdate)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FoodWarehouseLuxuryTheme {
                    FoodWarehouseApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodWarehouseApp(vm: WarehouseViewModel = viewModel()) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<FoodItemEntity?>(null) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var showBulkMovement by remember { mutableStateOf(false) }

    val summary by vm.summary.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val allItems by vm.allItems.collectAsStateWithLifecycle()
    val filteredItems by vm.filteredItems.collectAsStateWithLifecycle()
    val movements by vm.movements.collectAsStateWithLifecycle()
    val selectedCategoryId by vm.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedFilter by vm.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val updateState by vm.updateState.collectAsStateWithLifecycle()

    val tabs = AppTab.entries

    Scaffold(
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "انبار لوکس مواد غذایی",
                        color = ChampagneGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuxurySurface
                )
            )
        },
        floatingActionButton = {
            if (currentTab == AppTab.DASHBOARD.ordinal || currentTab == AppTab.INVENTORY.ordinal) {
                FloatingActionButton(
                    onClick = {
                        editingItem = null
                        showAddDialog = true
                    },
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBackground
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "افزودن محصول")
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = LuxurySurface) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChampagneGold,
                            selectedTextColor = ChampagneGold,
                            unselectedIconColor = TextSecondaryLuxury,
                            unselectedTextColor = TextSecondaryLuxury,
                            indicatorColor = ChampagneGold.copy(alpha = 0.14f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val mod = Modifier.padding(innerPadding)
        when (tabs[currentTab]) {
            AppTab.DASHBOARD -> DashboardScreen(
                summary = summary,
                items = allItems,
                categories = categories,
                onSelectFilterAndOpenInventory = { filter ->
                    vm.setFilter(filter)
                    currentTab = AppTab.INVENTORY.ordinal
                },
                onExportCsv = vm::exportCsvReport,
                onShareSummary = vm::shareTextSummary,
                onExportBackup = vm::exportJsonBackup,
                modifier = mod
            )

            AppTab.INVENTORY -> InventoryScreen(
                items = filteredItems,
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                selectedFilter = selectedFilter,
                searchQuery = searchQuery,
                onSearchQueryChange = vm::setSearchQuery,
                onCategorySelect = vm::setCategoryId,
                onFilterSelect = vm::setFilter,
                onOpenCategoryManager = { showCategoryManager = true },
                onOpenBulkMovement = { showBulkMovement = true },
                onEditItem = { item ->
                    editingItem = item
                    showAddDialog = true
                },
                onDeleteItem = vm::deleteItem,
                onAdjustStock = { item, type, delta ->
                    vm.recordMovement(item, type, delta)
                },
                modifier = mod
            )

            AppTab.ANALYTICS -> AnalyticsScreen(
                items = allItems,
                categories = categories,
                movements = movements,
                modifier = mod
            )

            AppTab.UPDATE -> SettingsUpdateScreen(
                updateState = updateState,
                onCheckUpdate = vm::checkForUpdates,
                onDownloadApk = vm::downloadUpdateApk,
                onInstallApk = vm::triggerInstallApk,
                modifier = mod
            )
        }

        if (showAddDialog) {
            AddEditItemDialog(
                initialItem = editingItem,
                categories = categories,
                onDismiss = {
                    showAddDialog = false
                    editingItem = null
                },
                onSave = { item, isNew ->
                    vm.addOrUpdateItem(item, isNew)
                    showAddDialog = false
                    editingItem = null
                }
            )
        }

        if (showCategoryManager) {
            CategoryManagementDialog(
                categories = categories,
                onAddCategory = vm::addCategory,
                onRenameCategory = vm::renameCategory,
                onDeleteCategory = vm::deleteCategory,
                onDismiss = { showCategoryManager = false }
            )
        }

        if (showBulkMovement) {
            BulkMovementDialog(
                items = allItems,
                onDismiss = { showBulkMovement = false },
                onApplyBulk = { entries, type, note ->
                    vm.applyBulkMovements(entries, type, note)
                }
            )
        }
    }
}
