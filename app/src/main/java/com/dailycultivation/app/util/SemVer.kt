package com.dailycultivation.app.util

/**
 * 语义化版本解析与比较。
 * 支持 "0.1.0" 和 "v0.1.0" 两种格式。
 */
object SemVer {

    data class Version(
        val major: Int,
        val minor: Int,
        val patch: Int,
    ) : Comparable<Version> {

        override fun compareTo(other: Version): Int {
            major.compareTo(other.major).let { if (it != 0) return it }
            minor.compareTo(other.minor).let { if (it != 0) return it }
            return patch.compareTo(other.patch)
        }

        override fun toString(): String = "$major.$minor.$patch"
    }

    fun parse(version: String): Version? {
        val trimmed = version.trimStart('v', 'V')
        val parts = trimmed.split(".")
        if (parts.size < 2 || parts.size > 3) return null
        return try {
            Version(
                major = parts[0].toInt(),
                minor = parts[1].toInt(),
                patch = parts.getOrElse(2) { "0" }.toInt(),
            )
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun isNewer(current: String, latest: String): Boolean {
        val currentVer = parse(current) ?: return false
        val latestVer = parse(latest) ?: return false
        return latestVer > currentVer
    }
}
