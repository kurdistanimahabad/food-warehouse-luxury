package com.luxury.foodwarehouse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.data.StockMovementEntity
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.MovementType
import com.luxury.foodwarehouse.ui.components.CategoryValueDonutChart
import com.luxury.foodwarehouse.ui.components.ExpiryStatusBarChart
import com.luxury.foodwarehouse.ui.components.LuxuryCard
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.CrimsonDanger
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury

@Composable
fun AnalyticsScreen(
    items: List<FoodItemEntity>,
    movements: List<StockMovementEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CategoryValueDonutChart(items = items)
        }

        item {
            ExpiryStatusBarChart(items = items)
        }

        item {
            Text(
                text = "گردش اخیر انبار (ورود، خروج و ضایعات)",
                color = TextPrimaryLuxury,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(movements, key = { it.id }) { mov ->
            val color = when (mov.movementType) {
                MovementType.INBOUND -> EmeraldHealthy
                MovementType.OUTBOUND -> ChampagneGold
                MovementType.WASTE -> CrimsonDanger
            }

            LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${mov.movementType.titleFa} • ${mov.itemName}",
                            color = TextPrimaryLuxury,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "مقدار: ${mov.quantity} | ${Formatters.formatDate(mov.timestampMillis)}",
                            color = TextSecondaryLuxury,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = Formatters.formatCurrency(mov.totalAmount),
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
