package com.dailycultivation.app.data.network

import com.dailycultivation.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * 通过 GitHub API 检查最新 Release。
 * 使用 HttpURLConnection，不引入第三方网络库。
 */
object UpdateChecker {

    private const val API_URL =
        "https://api.github.com/repos/${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}/releases/latest"

    /**
     * 查询最新 Release。如果没有 Release 或网络错误，返回 null / 抛异常。
     */
    suspend fun checkForUpdate(): Result<ReleaseInfo?> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(API_URL).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val json = connection.inputStream.bufferedReader().use { it.readText() }
                    val releaseInfo = parseReleaseJson(json)
                    Result.success(releaseInfo)
                }
                404 -> {
                    // 还没有任何 Release
                    Result.success(null)
                }
                403 -> {
                    Result.failure(IOException("API 请求过于频繁，请稍后再试"))
                }
                else -> {
                    Result.failure(IOException("服务器返回错误: ${connection.responseCode}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    private fun parseReleaseJson(json: String): ReleaseInfo? {
        val root = JSONObject(json)
        val tagName = root.getString("tag_name")
        val versionName = tagName.trimStart('v', 'V')

        // 找 .apk 文件
        val assets = root.getJSONArray("assets")
        var apkUrl: String? = null
        var apkSize: Long = 0

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                apkUrl = asset.getString("browser_download_url")
                apkSize = asset.getLong("size")
                break
            }
        }

        if (apkUrl == null) return null

        return ReleaseInfo(
            tagName = tagName,
            versionName = versionName,
            name = root.optString("name", tagName),
            body = root.optString("body", ""),
            apkUrl = apkUrl,
            apkSize = apkSize,
            publishedAt = root.optString("published_at", ""),
        )
    }
}
