package com.luxury.foodwarehouse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxury.foodwarehouse.data.CategoryEntity
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.model.ExpiryStatus
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.MovementType
import com.luxury.foodwarehouse.ui.components.BarcodeScannerDialog
import com.luxury.foodwarehouse.ui.components.LuxuryCard
import com.luxury.foodwarehouse.ui.theme.AmberWarning
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.CrimsonDanger
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury
import com.luxury.foodwarehouse.viewmodel.InventoryFilter

@Composable
fun InventoryScreen(
    items: List<FoodItemEntity>,
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    selectedFilter: InventoryFilter,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (Long?) -> Unit,
    onFilterSelect: (InventoryFilter) -> Unit,
    onOpenCategoryManager: () -> Unit,
    onOpenBulkMovement: () -> Unit,
    onEditItem: (FoodItemEntity) -> Unit,
    onDeleteItem: (FoodItemEntity) -> Unit,
    onAdjustStock: (FoodItemEntity, MovementType, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchScanner by remember { mutableStateOf(false) }
    val categoryNameById = remember(categories) { categories.associate { it.id to it.name } }

    if (showSearchScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { scanned ->
                onSearchQueryChange(scanned)
                showSearchScanner = false
            },
            onDismiss = { showSearchScanner = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("جستجو نام، بچ، بارکد یا انبار...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ChampagneGold) },
            trailingIcon = {
                IconButton(onClick = { showSearchScanner = true }) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = "اسکن بارکد", tint = ChampagneGold)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenCategoryManager,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Category, contentDescription = null, tint = ChampagneGold)
                Text(" دسته‌بندی‌ها", color = SoftGold, fontSize = 12.sp, maxLines = 1)
            }
            OutlinedButton(
                onClick = onOpenBulkMovement,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.PlaylistAddCheck, contentDescription = null, tint = EmeraldHealthy)
                Text(" ورود/خروج گروهی", color = TextPrimaryLuxury, fontSize = 12.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                LuxuryFilterChip(
                    selected = selectedCategoryId == null,
                    label = "همه دسته‌ها",
                    onClick = { onCategorySelect(null) }
                )
            }
            items(categories, key = { it.id }) { cat ->
                LuxuryFilterChip(
                    selected = selectedCategoryId == cat.id,
                    label = cat.name,
                    onClick = { onCategorySelect(cat.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filterOptions = listOf(
                InventoryFilter.ALL to "همه وضعیت‌ها",
                InventoryFilter.EXPIRING_SOON to "نزدیک انقضا",
                InventoryFilter.EXPIRED to "منقضی‌شده",
                InventoryFilter.LOW_STOCK to "کم‌موجود"
            )
            items(filterOptions) { (f, label) ->
                LuxuryFilterChip(
                    selected = selectedFilter == f,
                    label = label,
                    onClick = { onFilterSelect(f) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val status = Formatters.getExpiryStatus(item.expiryDateMillis)
                val statusColor = when (status) {
                    ExpiryStatus.HEALTHY -> EmeraldHealthy
                    ExpiryStatus.EXPIRING_SOON -> AmberWarning
                    ExpiryStatus.EXPIRED -> CrimsonDanger
                }
                val catTitle = categoryNameById[item.categoryId] ?: "سایر اقلام"

                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    highlightGold = status == ExpiryStatus.EXPIRING_SOON
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = TextPrimaryLuxury,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$catTitle • ${item.storageLocation}",
                                    color = TextSecondaryLuxury,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = status.labelFa,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "موجودی: ${Formatters.formatQty(item.quantity, item.unit)}",
                                color = SoftGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "ارزش کل: ${Formatters.formatCurrency(item.totalPurchaseValue)}",
                                color = TextPrimaryLuxury,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "تعداد در کارتن: ${item.unitsPerCarton} عدد • خرید کارتن: ${Formatters.formatCurrency(item.cartonPurchasePrice)} | فروش کارتن: ${Formatters.formatCurrency(item.cartonSalePrice)}",
                            color = SoftGold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "دونه خرید: ${Formatters.formatCurrency(item.purchasePrice)} | دونه فروش: ${Formatters.formatCurrency(item.salePrice)}",
                            color = TextSecondaryLuxury,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "تولید: ${Formatters.formatDate(item.productionDateMillis)} | انقضا: ${Formatters.formatDate(item.expiryDateMillis)} | بچ: ${item.batchNumber.ifBlank { "-" }}",
                            color = TextSecondaryLuxury,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onAdjustStock(item, MovementType.INBOUND, 1.0) }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "افزایش موجودی", tint = EmeraldHealthy)
                                }
                                IconButton(onClick = { onAdjustStock(item, MovementType.OUTBOUND, 1.0) }) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "خروج/فروش", tint = AmberWarning)
                                }
                                IconButton(onClick = { onAdjustStock(item, MovementType.WASTE, 1.0) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "ثبت ضایعات", tint = CrimsonDanger)
                                }
                            }
                            Row {
                                IconButton(onClick = { onEditItem(item) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "ویرایش", tint = ChampagneGold)
                                }
                                IconButton(onClick = { onDeleteItem(item) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف ردیف", tint = TextSecondaryLuxury)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LuxuryFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ChampagneGold,
            selectedLabelColor = ObsidianBackground
        )
    )
}
