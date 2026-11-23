package com.luxury.foodwarehouse.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.luxury.foodwarehouse.model.GitHubReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(private val context: Context) {

    suspend fun fetchLatestRelease(owner: String, repo: String): Result<GitHubReleaseInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val endpoint = "https://api.github.com/repos/$owner/$repo/releases/latest"
                val url = URL(endpoint)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "FoodWarehouseLuxury-Android")
                    connectTimeout = 12_000
                    readTimeout = 12_000
                }

                if (connection.responseCode !in 200..299) {
                    error("خطا در دریافت اطلاعات بروزرسانی (کد ${connection.responseCode})")
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)

                val tagName = json.optString("tag_name", "v1.0.0")
                val name = json.optString("name", tagName)
                val notes = json.optString("body", "بهبود عملکرد و رفع باگ‌ها")
                val publishedAt = json.optString("published_at", "")

                var apkUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i)
                        val downloadUrl = asset?.optString("browser_download_url")
                        if (downloadUrl != null && downloadUrl.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = downloadUrl
                            break
                        }
                    }
                }

                GitHubReleaseInfo(
                    tagName = tagName,
                    title = name,
                    releaseNotes = notes,
                    apkDownloadUrl = apkUrl,
                    publishedAt = publishedAt
                )
            }
        }

    fun isNewerVersion(currentVersion: String, latestTag: String): Boolean {
        val currentParts = currentVersion.removePrefix("v").removePrefix("V")
            .split(".", "-")
            .mapNotNull { it.toIntOrNull() }
        val latestParts = latestTag.removePrefix("v").removePrefix("V")
            .split(".", "-")
            .mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun enqueueApkDownload(apkUrl: String, fileName: String = "FoodWarehouseUpdate.apk"): Long {
        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("بروزرسانی انبار لوکس مواد غذایی")
            .setDescription("در حال دانلود نسخه جدید برنامه...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }

    fun installApkFromDownloads(fileName: String = "FoodWarehouseUpdate.apk") {
        val apkFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        if (!apkFile.exists()) return

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }
}
