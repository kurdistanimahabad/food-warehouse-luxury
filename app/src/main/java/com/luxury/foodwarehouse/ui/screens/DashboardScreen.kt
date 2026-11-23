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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.model.ExpiryStatus
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.WarehouseSummary
import com.luxury.foodwarehouse.ui.components.CategoryValueDonutChart
import com.luxury.foodwarehouse.ui.components.LuxuryCard
import com.luxury.foodwarehouse.ui.components.MetricStatCard
import com.luxury.foodwarehouse.ui.components.TotalInventoryBanner
import com.luxury.foodwarehouse.ui.theme.AmberWarning
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.CrimsonDanger
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury

@Composable
fun DashboardScreen(
    summary: WarehouseSummary,
    items: List<FoodItemEntity>,
    onExportCsv: () -> Unit,
    onShareSummary: () -> Unit,
    onExportBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgentItems = items
        .filter { Formatters.getExpiryStatus(it.expiryDateMillis) != ExpiryStatus.HEALTHY }
        .sortedBy { it.expiryDateMillis }
        .take(5)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TotalInventoryBanner(
                totalPurchaseValueText = Formatters.formatCurrency(summary.totalPurchaseValue),
                totalSaleValueText = Formatters.formatCurrency(summary.totalSaleValue),
                estimatedProfitText = Formatters.formatCurrency(summary.estimatedProfit)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.weight(1f)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = null,
                        tint = ChampagneGold
                    )
                    Text(" خروجی CSV", color = SoftGold, fontSize = 11.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onShareSummary,
                    modifier = Modifier.weight(1f)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = ChampagneGold
                    )
                    Text(" گزارش متن", color = SoftGold, fontSize = 11.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onExportBackup,
                    modifier = Modifier.weight(1f)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Backup,
                        contentDescription = null,
                        tint = ChampagneGold
                    )
                    Text(" بکاپ JSON", color = SoftGold, fontSize = 11.sp, maxLines = 1)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "تنوع اقلام (SKU)",
                    value = "${summary.totalSkuCount} قلم",
                    icon = Icons.Rounded.Inventory2,
                    accentColor = ChampagneGold,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "نزدیک انقضا",
                    value = "${summary.expiringSoonCount} قلم",
                    icon = Icons.Rounded.Schedule,
                    accentColor = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "منقضی‌شده",
                    value = "${summary.expiredCount} قلم",
                    icon = Icons.Rounded.ErrorOutline,
                    accentColor = CrimsonDanger,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "کم‌موجود",
                    value = "${summary.lowStockCount} قلم",
                    icon = Icons.Rounded.WarningAmber,
                    accentColor = EmeraldHealthy,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CategoryValueDonutChart(items = items)
        }

        item {
            Text(
                text = "اولویت‌های فوری انقضا (FEFO)",
                color = TextPrimaryLuxury,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (urgentItems.isEmpty()) {
            item {
                LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "عالی! هیچ کالای منقضی‌شده یا نزدیک به انقضا وجود ندارد.",
                        color = EmeraldHealthy,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(urgentItems, key = { it.id }) { item ->
                val status = Formatters.getExpiryStatus(item.expiryDateMillis)
                val badgeColor = if (status == ExpiryStatus.EXPIRED) CrimsonDanger else AmberWarning
                val daysLeft = Formatters.daysUntil(item.expiryDateMillis)

                LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = TextPrimaryLuxury,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "موجودی: ${Formatters.formatQty(item.quantity, item.unit)} • انقضا: ${Formatters.formatDate(item.expiryDateMillis)}",
                                color = TextSecondaryLuxury,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = if (daysLeft < 0) "${-daysLeft} روز گذشته" else "$daysLeft روز مانده",
                            color = badgeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
