package com.luxury.foodwarehouse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.model.MovementType
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
import com.luxury.foodwarehouse.ui.theme.LuxurySurface
import com.luxury.foodwarehouse.ui.theme.LuxurySurfaceElevated
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury

@Composable
fun BulkMovementDialog(
    items: List<FoodItemEntity>,
    onDismiss: () -> Unit,
    onApplyBulk: (entries: List<Pair<FoodItemEntity, Double>>, type: MovementType, note: String) -> Unit
) {
    var mode by remember { mutableStateOf(MovementType.OUTBOUND) }
    var note by remember { mutableStateOf("") }
    val qtyMap = remember { mutableStateMapOf<Long, String>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LuxurySurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "عملیات گروهی ورود و خروج کالا",
                    color = ChampagneGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == MovementType.OUTBOUND,
                        onClick = { mode = MovementType.OUTBOUND },
                        label = { Text("خروج / فروش گروهی", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGold,
                            selectedLabelColor = ObsidianBackground
                        )
                    )
                    FilterChip(
                        selected = mode == MovementType.INBOUND,
                        onClick = { mode = MovementType.INBOUND },
                        label = { Text("ورود گروهی به انبار", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldHealthy,
                            selectedLabelColor = ObsidianBackground
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("توضیحات فاکتور یا حواله (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "برای هر قلمی که می‌خواهید ثبت کنید، مقدار را وارد نمایید:",
                    color = TextSecondaryLuxury,
                    fontSize = 12.sp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val currentInput = qtyMap[item.id] ?: ""
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = LuxurySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        color = TextPrimaryLuxury,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "موجودی فعلی: ${Formatters.formatQty(item.quantity, item.unit)}",
                                        color = SoftGold,
                                        fontSize = 11.sp
                                    )
                                }
                                OutlinedTextField(
                                    value = currentInput,
                                    onValueChange = { qtyMap[item.id] = it },
                                    label = { Text(item.unit, fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.width(92.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("انصراف", color = TextSecondaryLuxury)
                    }
                    Button(
                        onClick = {
                            val selectedEntries = items.mapNotNull { item ->
                                val raw = qtyMap[item.id]?.trim() ?: ""
                                val delta = raw.toDoubleOrNull()
                                if (delta != null && delta > 0.0) item to delta else null
                            }
                            if (selectedEntries.isNotEmpty()) {
                                onApplyBulk(selectedEntries, mode, note.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ثبت گروهی", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
