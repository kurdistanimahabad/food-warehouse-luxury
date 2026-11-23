package com.luxury.foodwarehouse.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.luxury.foodwarehouse.data.CategoryEntity
import com.luxury.foodwarehouse.model.JalaliCalendar
import com.luxury.foodwarehouse.model.JalaliDate
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.CrimsonDanger
import com.luxury.foodwarehouse.ui.theme.LuxurySurface
import com.luxury.foodwarehouse.ui.theme.LuxurySurfaceElevated
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury
import java.util.concurrent.TimeUnit

@Composable
fun JalaliDatePickerDialog(
    title: String,
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val initialJalali = remember(initialMillis) { JalaliCalendar.millisToJalali(initialMillis) }
    var year by remember { mutableIntStateOf(initialJalali.year) }
    var month by remember { mutableIntStateOf(initialJalali.month) }
    var day by remember {
        mutableIntStateOf(
            initialJalali.day.coerceIn(1, JalaliCalendar.daysInJalaliMonth(initialJalali.year, initialJalali.month))
        )
    }

    val maxDayInCurrentMonth = JalaliCalendar.daysInJalaliMonth(year, month)
    if (day > maxDayInCurrentMonth) {
        day = maxDayInCurrentMonth
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LuxurySurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    color = ChampagneGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "تاریخ انتخاب‌شده: ${JalaliDate(year, month, day).formatted()} (${JalaliCalendar.monthNamesFa[month - 1]})",
                    color = SoftGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // میانبرهای سریع تاریخ شمسی
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val shortcuts = listOf(
                        "امروز" to 0L,
                        "+۳۰ روز" to TimeUnit.DAYS.toMillis(30),
                        "+۹۰ روز" to TimeUnit.DAYS.toMillis(90),
                        "+۱ سال" to TimeUnit.DAYS.toMillis(365)
                    )
                    shortcuts.forEach { (label, offset) ->
                        OutlinedButton(
                            onClick = {
                                val target = JalaliCalendar.millisToJalali(System.currentTimeMillis() + offset)
                                year = target.year
                                month = target.month
                                day = target.day.coerceIn(1, JalaliCalendar.daysInJalaliMonth(target.year, target.month))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = label, fontSize = 10.sp, color = TextPrimaryLuxury, maxLines = 1)
                        }
                    }
                }

                // کنترل سال شمسی
                StepperRow(
                    label = "سال شمسی",
                    valueText = "$year",
                    onDecrement = { year = (year - 1).coerceAtLeast(1350) },
                    onIncrement = { year = (year + 1).coerceAtMost(1450) }
                )

                // کنترل ماه شمسی
                StepperRow(
                    label = "ماه شمسی",
                    valueText = "$month - ${JalaliCalendar.monthNamesFa[month - 1]}",
                    onDecrement = {
                        if (month > 1) month-- else {
                            month = 12
                            year = (year - 1).coerceAtLeast(1350)
                        }
                    },
                    onIncrement = {
                        if (month < 12) month++ else {
                            month = 1
                            year = (year + 1).coerceAtMost(1450)
                        }
                    }
                )

                // کنترل روز شمسی
                StepperRow(
                    label = "روز ماه",
                    valueText = "$day از $maxDayInCurrentMonth",
                    onDecrement = { day = if (day > 1) day - 1 else maxDayInCurrentMonth },
                    onIncrement = { day = if (day < maxDayInCurrentMonth) day + 1 else 1 }
                )

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
                            val millis = JalaliCalendar.jalaliToMillis(JalaliDate(year, month, day), initialMillis)
                            onDateSelected(millis)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تایید تاریخ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = LuxurySurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondaryLuxury, fontSize = 12.sp, modifier = Modifier.width(74.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) {
                    Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "کاهش", tint = ChampagneGold)
                }
                Text(
                    text = valueText,
                    color = TextPrimaryLuxury,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = "افزایش", tint = ChampagneGold)
                }
            }
        }
    }
}

@Composable
fun CategoryManagementDialog(
    categories: List<CategoryEntity>,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (CategoryEntity, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var editingTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var editNameText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LuxurySurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "مدیریت دسته‌بندی محصولات",
                    color = ChampagneGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("نام دسته‌بندی جدید") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                onAddCategory(newCategoryName)
                                newCategoryName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBackground
                        )
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "افزودن")
                    }
                }

                if (editingTarget != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = LuxurySurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ویرایش نام دسته: ${editingTarget!!.name}",
                                color = SoftGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = editNameText,
                                onValueChange = { editNameText = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { editingTarget = null }) {
                                    Text("لغو", color = TextSecondaryLuxury, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val target = editingTarget
                                        if (target != null && editNameText.isNotBlank()) {
                                            onRenameCategory(target, editNameText)
                                            editingTarget = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ChampagneGold,
                                        contentColor = ObsidianBackground
                                    )
                                ) {
                                    Text("ثبت تغییر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LuxurySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cat.name,
                                    color = TextPrimaryLuxury,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    editingTarget = cat
                                    editNameText = cat.name
                                }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "ویرایش", tint = ChampagneGold)
                                }
                                if (categories.size > 1) {
                                    IconButton(onClick = { onDeleteCategory(cat) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = CrimsonDanger)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بستن", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
