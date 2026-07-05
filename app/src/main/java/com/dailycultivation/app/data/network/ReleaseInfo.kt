package com.dailycultivation.app.data.network

/**
 * 从 GitHub Release API 解析出的版本信息。
 */
data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val name: String,
    val body: String,
    val apkUrl: String,
    val apkSize: Long,
    val publishedAt: String,
)
