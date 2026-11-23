package com.luxury.foodwarehouse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.luxury.foodwarehouse.data.CategoryEntity
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.model.Formatters
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.LuxurySurface
import com.luxury.foodwarehouse.ui.theme.LuxurySurfaceElevated
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury
import java.util.concurrent.TimeUnit

private enum class ActiveDateTarget {
    PRODUCTION, EXPIRY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    initialItem: FoodItemEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (FoodItemEntity, Boolean) -> Unit
) {
    val now = remember { System.currentTimeMillis() }

    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var batchNumber by remember { mutableStateOf(initialItem?.batchNumber ?: "") }
    var barcode by remember { mutableStateOf(initialItem?.barcode ?: "") }

    val fallbackCategory = categories.firstOrNull()
    var selectedCategory by remember {
        mutableStateOf(
            categories.firstOrNull { it.id == initialItem?.categoryId } ?: fallbackCategory
        )
    }

    var unit by remember { mutableStateOf(initialItem?.unit ?: "کارتن") }
    var unitsPerCartonText by remember { mutableStateOf((initialItem?.unitsPerCarton ?: 12).toString()) }
    var quantityText by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "1") }
    var minStockText by remember { mutableStateOf(initialItem?.minStockAlert?.toString() ?: "5") }
    var purchasePriceText by remember { mutableStateOf(initialItem?.purchasePrice?.toString() ?: "100000") }
    var salePriceText by remember { mutableStateOf(initialItem?.salePrice?.toString() ?: "125000") }
    var storageLocation by remember { mutableStateOf(initialItem?.storageLocation ?: "انبار مرکزی") }
    var supplierName by remember { mutableStateOf(initialItem?.supplierName ?: "") }

    var prodDateMillis by remember {
        mutableLongStateOf(initialItem?.productionDateMillis ?: now)
    }
    var expDateMillis by remember {
        mutableLongStateOf(initialItem?.expiryDateMillis ?: (now + TimeUnit.DAYS.toMillis(90)))
    }

    var expandedCategory by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<ActiveDateTarget?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedUnitsPerCarton = unitsPerCartonText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val parsedUnitBuy = purchasePriceText.toLongOrNull() ?: 0L
    val parsedUnitSell = salePriceText.toLongOrNull() ?: 0L
    val calculatedCartonBuy = parsedUnitBuy * parsedUnitsPerCarton
    val calculatedCartonSell = parsedUnitSell * parsedUnitsPerCarton

    if (showScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { scanned ->
                barcode = scanned
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }

    if (datePickerTarget != null) {
        val isExpiry = datePickerTarget == ActiveDateTarget.EXPIRY
        JalaliDatePickerDialog(
            title = if (isExpiry) "انتخاب تاریخ انقضا (شمسی)" else "انتخاب تاریخ تولید (شمسی)",
            initialMillis = if (isExpiry) expDateMillis else prodDateMillis,
            onDismiss = { datePickerTarget = null },
            onDateSelected = { chosenMillis ->
                if (isExpiry) expDateMillis = chosenMillis else prodDateMillis = chosenMillis
                datePickerTarget = null
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LuxurySurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (initialItem == null) "ثبت محصول جدید مواد غذایی" else "ویرایش اطلاعات محصول",
                    color = ChampagneGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام محصول *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "سایر اقلام",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("موجودی *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("واحد") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = unitsPerCartonText,
                    onValueChange = { unitsPerCartonText = it },
                    label = { Text("تعداد در کارتن (مثلاً ۱۲ یا ۲۴)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("قیمت خرید هر دونه *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salePriceText,
                        onValueChange = { salePriceText = it },
                        label = { Text("قیمت فروش هر دونه *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LuxurySurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "محاسبه خودکار قیمت کارتن ($parsedUnitsPerCarton عدد در کارتن):",
                            color = SoftGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "قیمت خرید هر کارتن: ${Formatters.formatCurrency(calculatedCartonBuy)}",
                            color = TextPrimaryLuxury,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "قیمت فروش هر کارتن: ${Formatters.formatCurrency(calculatedCartonSell)}",
                            color = TextPrimaryLuxury,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = Formatters.formatDate(prodDateMillis),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تولید (شمسی)") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerTarget = ActiveDateTarget.PRODUCTION }) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = "تاریخ تولید", tint = ChampagneGold)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerTarget = ActiveDateTarget.PRODUCTION }
                    )
                    OutlinedTextField(
                        value = Formatters.formatDate(expDateMillis),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("انقضا (شمسی)") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerTarget = ActiveDateTarget.EXPIRY }) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = "تاریخ انقضا", tint = ChampagneGold)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerTarget = ActiveDateTarget.EXPIRY }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("سری ساخت / بچ") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("حداقل موجودی") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = storageLocation,
                    onValueChange = { storageLocation = it },
                    label = { Text("محل نگهداری (سردخانه / انبار خشک)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("تأمین‌کننده") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("کد بارکد") },
                    trailingIcon = {
                        IconButton(onClick = { showScanner = true }) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = "اسکن بارکد",
                                tint = ChampagneGold
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                            val qty = quantityText.toDoubleOrNull()
                            val buy = purchasePriceText.toLongOrNull()
                            val sell = salePriceText.toLongOrNull()
                            val minS = minStockText.toDoubleOrNull() ?: 5.0
                            val perCarton = unitsPerCartonText.toIntOrNull()

                            when {
                                name.isBlank() -> errorMessage = "نام محصول را وارد کنید."
                                qty == null || qty < 0 -> errorMessage = "مقدار موجودی معتبر نیست."
                                perCarton == null || perCarton <= 0 -> errorMessage = "تعداد در کارتن باید حداقل ۱ باشد."
                                buy == null || buy < 0 -> errorMessage = "قیمت خرید هر دونه معتبر نیست."
                                sell == null || sell < 0 -> errorMessage = "قیمت فروش هر دونه معتبر نیست."
                                else -> {
                                    val entity = FoodItemEntity(
                                        id = initialItem?.id ?: 0L,
                                        name = name.trim(),
                                        barcode = barcode.trim(),
                                        batchNumber = batchNumber.trim(),
                                        categoryId = selectedCategory?.id ?: 1L,
                                        unit = unit.trim().ifBlank { "کارتن" },
                                        unitsPerCarton = perCarton,
                                        quantity = qty,
                                        minStockAlert = minS,
                                        purchasePrice = buy,
                                        salePrice = sell,
                                        storageLocation = storageLocation.trim().ifBlank { "انبار مرکزی" },
                                        productionDateMillis = prodDateMillis,
                                        expiryDateMillis = expDateMillis,
                                        supplierName = supplierName.trim()
                                    )
                                    onSave(entity, initialItem == null)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ذخیره", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
