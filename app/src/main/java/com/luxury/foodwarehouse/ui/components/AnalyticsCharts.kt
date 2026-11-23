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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.model.ExpiryStatus
import com.luxury.foodwarehouse.model.FoodCategory
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.ui.theme.AmberWarning
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.CrimsonDanger
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
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
    Color(0xFFF97316)
)

@Composable
fun CategoryValueDonutChart(
    items: List<FoodItemEntity>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = FoodCategory.entries.mapIndexed { idx, cat ->
        val value = items.filter { it.category == cat }.sumOf { it.totalPurchaseValue }
        Triple(cat, value, CategoryPalette[idx % CategoryPalette.size])
    }.filter { it.second > 0L }

    val grandTotal = categoryTotals.sumOf { it.second }.coerceAtLeast(1L)

    LuxuryCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "سهم ارزش مالی هر دسته‌بندی",
                color = TextPrimaryLuxury,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "بر اساس قیمت خرید و موجودی لحظه‌ای",
                color = TextSecondaryLuxury,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(18.dp))

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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(136.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(130.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 24.dp.toPx()
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
                        categoryTotals.forEach { (cat, value, color) ->
                            val percent = ((value.toDouble() / grandTotal.toDouble()) * 100).toInt()
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
                                        text = cat.titleFa,
                                        color = TextPrimaryLuxury,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "$percent%",
                                    color = TextSecondaryLuxury,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
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
