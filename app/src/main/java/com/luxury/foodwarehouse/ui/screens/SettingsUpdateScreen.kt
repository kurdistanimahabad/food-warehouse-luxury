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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxury.foodwarehouse.BuildConfig
import com.luxury.foodwarehouse.ui.components.LuxuryCard
import com.luxury.foodwarehouse.ui.theme.ChampagneGold
import com.luxury.foodwarehouse.ui.theme.EmeraldHealthy
import com.luxury.foodwarehouse.ui.theme.ObsidianBackground
import com.luxury.foodwarehouse.ui.theme.SoftGold
import com.luxury.foodwarehouse.ui.theme.TextPrimaryLuxury
import com.luxury.foodwarehouse.ui.theme.TextSecondaryLuxury
import com.luxury.foodwarehouse.viewmodel.UpdateUiState

@Composable
fun SettingsUpdateScreen(
    updateState: UpdateUiState,
    onCheckUpdate: (owner: String, repo: String) -> Unit,
    onDownloadApk: (url: String) -> Unit,
    onInstallApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    var githubOwner by remember { mutableStateOf("your-github-username") }
    var githubRepo by remember { mutableStateOf("food-warehouse-luxury") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LuxuryCard(modifier = Modifier.fillMaxWidth(), highlightGold = true) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "بروزرسانی هوشمند نرم‌افزار (OTA / GitHub)",
                        color = ChampagneGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "نسخه فعلی نصب‌شده: ${BuildConfig.VERSION_NAME} (کد ${BuildConfig.VERSION_CODE})",
                        color = TextSecondaryLuxury,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = githubOwner,
                        onValueChange = { githubOwner = it },
                        label = { Text("نام کاربری یا سازمان GitHub") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = githubRepo,
                        onValueChange = { githubRepo = it },
                        label = { Text("نام مخزن (Repository)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onCheckUpdate(githubOwner, githubRepo) },
                        enabled = !updateState.isChecking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (updateState.isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = ObsidianBackground
                            )
                        }
                        Text(
                            text = if (updateState.isChecking) "در حال بررسی..." else "بررسی نسخه جدید",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (updateState.statusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = updateState.statusMessage,
                            color = SoftGold,
                            fontSize = 13.sp
                        )
                    }

                    val release = updateState.latestRelease
                    if (release != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "آخرین نسخه منتشرشده: ${release.tagName} - ${release.title}",
                            color = TextPrimaryLuxury,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = release.releaseNotes,
                            color = TextSecondaryLuxury,
                            fontSize = 12.sp
                        )

                        val apkUrl = release.apkDownloadUrl
                        if (updateState.updateAvailable && apkUrl != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onDownloadApk(apkUrl) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldHealthy,
                                        contentColor = ObsidianBackground
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("دانلود APK", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onInstallApk,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("نصب فایل", color = ChampagneGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
