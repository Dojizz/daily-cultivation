package com.dailycultivation.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * 将 APK 文件下载到本地缓存目录，支持进度回调。
 */
object ApkDownloader {

    /**
     * 下载 APK 到 [destFile]。回调 [onProgress] 提供 (已下载, 总大小) 的字节数。
     */
    suspend fun downloadApk(
        url: String,
        destFile: File,
        onProgress: ((downloaded: Long, total: Long) -> Unit)? = null,
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/octet-stream")
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    IOException("下载失败: HTTP ${connection.responseCode}")
                )
            }

            val totalBytes = connection.contentLengthLong

            // 删除旧文件
            if (destFile.exists()) {
                destFile.delete()
            }

            connection.inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var downloadedBytes = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        onProgress?.invoke(downloadedBytes, totalBytes)
                    }
                }
            }

            // 验证文件完整性
            if (totalBytes > 0 && destFile.length() != totalBytes) {
                destFile.delete()
                return@withContext Result.failure(IOException("下载文件不完整"))
            }

            Result.success(destFile)
        } catch (e: IOException) {
            // 清理下载了一半的文件
            if (destFile.exists()) {
                destFile.delete()
            }
            Result.failure(e)
        }
    }
}
