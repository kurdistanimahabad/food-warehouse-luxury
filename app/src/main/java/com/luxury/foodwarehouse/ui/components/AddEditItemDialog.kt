package com.luxury.foodwarehouse.ui.components

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
import com.luxury.foodwarehouse.data.FoodItemEntity
import com.luxury.foodwarehouse.model.FoodCategory
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.LuxurySurfaceElevated
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    initialItem: FoodItemEntity?,
    onDismiss: () -> Unit,
    onSave: (FoodItemEntity, Boolean) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }
    val now = remember { System.currentTimeMillis() }

    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var batchNumber by remember { mutableStateOf(initialItem?.batchNumber ?: "") }
    var barcode by remember { mutableStateOf(initialItem?.barcode ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: FoodCategory.DAIRY) }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "کارتن") }
    var quantityText by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "10") }
    var minStockText by remember { mutableStateOf(initialItem?.minStockAlert?.toString() ?: "5") }
    var purchasePriceText by remember { mutableStateOf(initialItem?.purchasePrice?.toString() ?: "1000000") }
    var salePriceText by remember { mutableStateOf(initialItem?.salePrice?.toString() ?: "1250000") }
    var storageLocation by remember { mutableStateOf(initialItem?.storageLocation ?: "انبار مرکزی") }
    var supplierName by remember { mutableStateOf(initialItem?.supplierName ?: "") }

    var prodDateText by remember {
        mutableStateOf(sdf.format(initialItem?.productionDateMillis ?: now))
    }
    var expDateText by remember {
        mutableStateOf(
            sdf.format(initialItem?.expiryDateMillis ?: (now + TimeUnit.DAYS.toMillis(90)))
        )
    }

    var expandedCategory by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { scanned ->
                barcode = scanned
                showScanner = false
            },
            onDismiss = { showScanner = false }
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
                        value = category.titleFa,
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
                        FoodCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.titleFa) },
                                onClick = {
                                    category = cat
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("قیمت خرید *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salePriceText,
                        onValueChange = { salePriceText = it },
                        label = { Text("قیمت فروش *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prodDateText,
                        onValueChange = { prodDateText = it },
                        label = { Text("تولید (YYYY/MM/DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expDateText,
                        onValueChange = { expDateText = it },
                        label = { Text("انقضا (YYYY/MM/DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
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
                            val prodMillis = runCatching { sdf.parse(prodDateText)?.time }.getOrNull() ?: now
                            val expMillis = runCatching { sdf.parse(expDateText)?.time }.getOrNull()

                            when {
                                name.isBlank() -> errorMessage = "نام محصول را وارد کنید."
                                qty == null || qty < 0 -> errorMessage = "مقدار موجودی معتبر نیست."
                                buy == null || buy < 0 -> errorMessage = "قیمت خرید معتبر نیست."
                                sell == null || sell < 0 -> errorMessage = "قیمت فروش معتبر نیست."
                                expMillis == null -> errorMessage = "فرمت تاریخ انقضا باید YYYY/MM/DD باشد."
                                else -> {
                                    val entity = FoodItemEntity(
                                        id = initialItem?.id ?: 0L,
                                        name = name.trim(),
                                        barcode = barcode.trim(),
                                        batchNumber = batchNumber.trim(),
                                        category = category,
                                        unit = unit.trim().ifBlank { "کارتن" },
                                        quantity = qty,
                                        minStockAlert = minS,
                                        purchasePrice = buy,
                                        salePrice = sell,
                                        storageLocation = storageLocation.trim().ifBlank { "انبار مرکزی" },
                                        productionDateMillis = prodMillis,
                                        expiryDateMillis = expMillis,
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
