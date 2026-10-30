package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class ReadyToInstall(val apkFile: File) : DownloadState()
    data class NeedsPermission(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class UpdateDownloader(private val context: Context) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun downloadApk(
        url: String,
        targetVersionCode: Int,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            onProgress(DownloadState.Error("L'adresse de téléchargement de l'APK n'est pas renseignée."))
            return@withContext
        }

        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val targetApk = File(updatesDir, "cinefamille_v$targetVersionCode.apk")
        val tempApk = File(updatesDir, "cinefamille_v$targetVersionCode.tmp")

        // Demo / simulation mode if URL is "demo" or "test"
        if (url.startsWith("demo", ignoreCase = true) || url.startsWith("test", ignoreCase = true)) {
            simulateDownload(targetApk, onProgress)
            return@withContext
        }

        try {
            onProgress(DownloadState.Downloading(0, 0L, 0L))
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) CineFamille/1.3")
                .header("Accept", "application/vnd.android.package-archive,application/octet-stream,*/*")
                .build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorMsg = if (response.code == 618) {
                    "Le lien de téléchargement temporaire a expiré (erreur GitHub 618). Utilisez le lien permanent GitHub Releases ou téléchargez via le navigateur."
                } else {
                    "Échec du téléchargement HTTP : code ${response.code}"
                }
                onProgress(DownloadState.Error(errorMsg))
                return@withContext
            }

            val body = response.body ?: run {
                onProgress(DownloadState.Error("Fichier vide reçu du serveur"))
                return@withContext
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input: InputStream ->
                FileOutputStream(tempApk).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var bytesRead: Int
                    var lastReportTime = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastReportTime >= 150) {
                            lastReportTime = now
                            val progressPercent = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                            } else {
                                50
                            }
                            onProgress(DownloadState.Downloading(progressPercent, downloadedBytes, totalBytes))
                        }
                    }
                    output.flush()
                }
            }

            if (tempApk.exists()) {
                if (targetApk.exists()) targetApk.delete()
                tempApk.renameTo(targetApk)
            }

            onProgress(DownloadState.Downloading(100, downloadedBytes, downloadedBytes))
            delay(200)
            onProgress(DownloadState.ReadyToInstall(targetApk))

        } catch (e: Exception) {
            if (tempApk.exists()) tempApk.delete()
            onProgress(DownloadState.Error("Erreur lors du téléchargement : ${e.localizedMessage ?: e.message}"))
        }
    }

    private suspend fun simulateDownload(
        targetApk: File,
        onProgress: (DownloadState) -> Unit
    ) {
        val totalBytes = 24_500_000L // 24.5 MB demo
        for (i in 0..100 step 10) {
            delay(120)
            val currentBytes = (totalBytes * i) / 100
            onProgress(DownloadState.Downloading(i, currentBytes, totalBytes))
        }
        // Write mock dummy file
        targetApk.writeText("Demo CineFamille APK update file")
        delay(150)
        onProgress(DownloadState.ReadyToInstall(targetApk))
    }

    fun canRequestInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun installApk(apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        if (!canRequestInstalls()) {
            openInstallPermissionSettings()
            return false
        }

        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
