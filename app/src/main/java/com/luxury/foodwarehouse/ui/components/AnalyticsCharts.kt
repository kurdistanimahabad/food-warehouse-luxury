package com.luxury.foodwarehouse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxury.foodwarehouse.data.CategoryEntity
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.data.StockMovementEntity
import com.luxury.foodwarehouse.model.ExpiryStatus
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.MovementType
import com.luxury.foodwarehouse.ui.theme.AmberWarning
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.CrimsonDanger
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.SapphireInfo
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury

private val CategoryPalette = listOf(
    ChampagneGold,
    EmeraldHealthy,
    SapphireInfo,
    AmberWarning,
    Color(0xFFA855F7),
    Color(0xFFF97316),
    Color(0xFF14B8A6),
    Color(0xFFF43F5E)
)

@Composable
fun CategoryValueDonutChart(
    items: List<FoodItemEntity>,
    categories: List<CategoryEntity>,
    modifier: Modifier = Modifier
) {
    var showMonetaryAmount by remember { mutableStateOf(false) }

    val categoryTotals = categories.mapIndexed { idx, cat ->
        val value = items.filter { it.categoryId == cat.id }.sumOf { it.totalPurchaseValue }
        Triple(cat.name, value, CategoryPalette[(cat.colorIndex.takeIf { it >= 0 } ?: idx) % CategoryPalette.size])
    }.filter { it.second > 0L }

    val grandTotal = categoryTotals.sumOf { it.second }.coerceAtLeast(1L)

    LuxuryCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سهم ارزش مالی هر دسته‌بندی",
                        color = TextPrimaryLuxury,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (showMonetaryAmount) "نمایش مبلغ خرید هر دسته" else "نمایش درصد سهم از کل انبار",
                        color = TextSecondaryLuxury,
                        fontSize = 11.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !showMonetaryAmount,
                        onClick = { showMonetaryAmount = false },
                        label = { Text("درصد", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGold,
                            selectedLabelColor = ObsidianBackground
                        )
                    )
                    FilterChip(
                        selected = showMonetaryAmount,
                        onClick = { showMonetaryAmount = true },
                        label = { Text("مبلغ", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGold,
                            selectedLabelColor = ObsidianBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryTotals.isEmpty()) {
                Text(
                    text = "هنوز کالایی برای نمایش نمودار ثبت نشده است.",
                    color = TextSecondaryLuxury,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(128.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(122.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 22.dp.toPx()
                            val inset = strokeWidth / 2f
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                            categoryTotals.forEach { (_, value, color) ->
                                val sweep = (value.toFloat() / grandTotal.toFloat()) * 360f
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = (sweep - 2f).coerceAtLeast(0f),
                                    useCenter = false,
                                    topLeft = Offset(inset, inset),
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${categoryTotals.size}",
                                color = SoftGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "دسته فعال",
                                color = TextSecondaryLuxury,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryTotals.forEach { (catName, value, color) ->
                            val percent = ((value.toDouble() / grandTotal.toDouble()) * 100).toInt()
                            val displayMetric = if (showMonetaryAmount) {
                                Formatters.formatCurrency(value)
                            } else {
                                "$percent%"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = catName,
                                        color = TextPrimaryLuxury,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayMetric,
                                    color = SoftGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesOutboundChart(
    items: List<FoodItemEntity>,
    categories: List<CategoryEntity>,
    movements: List<StockMovementEntity>,
    modifier: Modifier = Modifier
) {
    var groupByCategory by remember { mutableStateOf(true) }
    var metricByAmount by remember { mutableStateOf(true) }

    val outboundMovements = remember(movements) {
        movements.filter { it.movementType == MovementType.OUTBOUND }
    }

    val itemMap = remember(items) { items.associateBy { it.id } }
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    data class SalesRowData(
        val title: String,
        val amount: Long,
        val quantity: Double,
        val color: Color
    )

    val rows: List<SalesRowData> = remember(outboundMovements, itemMap, categoryMap, groupByCategory) {
        if (groupByCategory) {
            val grouped = mutableMapOf<String, Pair<Long, Double>>()
            outboundMovements.forEach { mov ->
                val catId = itemMap[mov.itemId]?.categoryId
                val catName = categoryMap[catId]?.name ?: "سایر اقلام"
                val prev = grouped[catName] ?: (0L to 0.0)
                grouped[catName] = (prev.first + mov.totalAmount) to (prev.second + mov.quantity)
            }
            grouped.entries
                .sortedByDescending { it.value.first }
                .mapIndexed { idx, (title, pair) ->
                    SalesRowData(
                        title = title,
                        amount = pair.first,
                        quantity = pair.second,
                        color = CategoryPalette[idx % CategoryPalette.size]
                    )
                }
        } else {
            val grouped = mutableMapOf<String, Pair<Long, Double>>()
            outboundMovements.forEach { mov ->
                val prev = grouped[mov.itemName] ?: (0L to 0.0)
                grouped[mov.itemName] = (prev.first + mov.totalAmount) to (prev.second + mov.quantity)
            }
            grouped.entries
                .sortedByDescending { it.value.first }
                .mapIndexed { idx, (title, pair) ->
                    SalesRowData(
                        title = title,
                        amount = pair.first,
                        quantity = pair.second,
                        color = CategoryPalette[idx % CategoryPalette.size]
                    )
                }
        }
    }

    val maxMetric = rows.maxOfOrNull { if (metricByAmount) it.amount.toDouble() else it.quantity }
        ?.coerceAtLeast(1.0) ?: 1.0

    LuxuryCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "نمودار فروش و خروج کالاها",
                color = TextPrimaryLuxury,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "تحلیل میزان خروج بر اساس دسته‌بندی یا اقلام",
                color = TextSecondaryLuxury,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = groupByCategory,
                    onClick = { groupByCategory = true },
                    label = { Text("بر اساس دسته‌بندی", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChampagneGold,
                        selectedLabelColor = ObsidianBackground
                    )
                )
                FilterChip(
                    selected = !groupByCategory,
                    onClick = { groupByCategory = false },
                    label = { Text("بر اساس اقلام", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChampagneGold,
                        selectedLabelColor = ObsidianBackground
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = metricByAmount,
                    onClick = { metricByAmount = true },
                    label = { Text("مبلغ فروش", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldHealthy,
                        selectedLabelColor = ObsidianBackground
                    )
                )
                FilterChip(
                    selected = !metricByAmount,
                    onClick = { metricByAmount = false },
                    label = { Text("تعداد/مقدار خروج", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldHealthy,
                        selectedLabelColor = ObsidianBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (rows.isEmpty()) {
                Text(
                    text = "هنوز حرکت خروجی/فروشی برای نمایش نمودار ثبت نشده است.",
                    color = TextSecondaryLuxury,
                    fontSize = 13.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        val currentVal = if (metricByAmount) row.amount.toDouble() else row.quantity
                        val fraction = (currentVal / maxMetric).toFloat().coerceIn(0.04f, 1f)
                        val labelRight = if (metricByAmount) {
                            Formatters.formatCurrency(row.amount)
                        } else {
                            "${Formatters.formatQty(row.quantity)} واحد (${Formatters.formatCurrency(row.amount)})"
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.title,
                                    color = TextPrimaryLuxury,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = labelRight,
                                    color = row.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(row.color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryStatusBarChart(
    items: List<FoodItemEntity>,
    modifier: Modifier = Modifier
) {
    val healthy = items.count { Formatters.getExpiryStatus(it.expiryDateMillis) == ExpiryStatus.HEALTHY }
    val soon = items.count { Formatters.getExpiryStatus(it.expiryDateMillis) == ExpiryStatus.EXPIRING_SOON }
    val expired = items.count { Formatters.getExpiryStatus(it.expiryDateMillis) == ExpiryStatus.EXPIRED }
    val total = items.size.coerceAtLeast(1)

    LuxuryCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "وضعیت سلامت و تاریخ انقضای اقلام",
                color = TextPrimaryLuxury,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            StatusProgressRow(
                label = "سالم و دارای تاریخ معتبر",
                count = healthy,
                fraction = healthy.toFloat() / total.toFloat(),
                color = EmeraldHealthy
            )
            Spacer(modifier = Modifier.height(10.dp))
            StatusProgressRow(
                label = "نزدیک به انقضا (اولویت فروش FEFO)",
                count = soon,
                fraction = soon.toFloat() / total.toFloat(),
                color = AmberWarning
            )
            Spacer(modifier = Modifier.height(10.dp))
            StatusProgressRow(
                label = "منقضی‌شده (نیاز به خروج/ضایعات)",
                count = expired,
                fraction = expired.toFloat() / total.toFloat(),
                color = CrimsonDanger
            )
        }
    }
}

@Composable
private fun StatusProgressRow(
    label: String,
    count: Int,
    fraction: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextPrimaryLuxury, fontSize = 12.sp)
            Text(text = "$count قلم", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}
