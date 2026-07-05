package com.dailycultivation.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailycultivation.app.BuildConfig
import com.dailycultivation.app.data.network.ApkDownloader
import com.dailycultivation.app.data.network.ReleaseInfo
import com.dailycultivation.app.data.network.UpdateChecker
import com.dailycultivation.app.util.ApkInstaller
import com.dailycultivation.app.util.SemVer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Available(val release: ReleaseInfo) : UpdateUiState
    data class Downloading(val progress: Float, val downloaded: Long, val total: Long) : UpdateUiState
    data class ReadyToInstall(val apkFile: File) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val apkFile: File
        get() = File(getApplication<Application>().cacheDir, "update.apk")

    fun checkUpdate() {
        if (_uiState.value is UpdateUiState.Downloading) return

        _uiState.value = UpdateUiState.Checking
        viewModelScope.launch {
            val result = UpdateChecker.checkForUpdate()
            result.fold(
                onSuccess = { releaseInfo ->
                    if (releaseInfo == null) {
                        _uiState.value = UpdateUiState.Error("暂无可用版本")
                        return@fold
                    }
                    val currentVersion = BuildConfig.VERSION_NAME
                    if (SemVer.isNewer(currentVersion, releaseInfo.versionName)) {
                        _uiState.value = UpdateUiState.Available(releaseInfo)
                    } else {
                        _uiState.value = UpdateUiState.UpToDate
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "检查更新失败", error)
                    _uiState.value = UpdateUiState.Error(
                        error.message ?: "网络连接失败，请检查网络后重试"
                    )
                },
            )
        }
    }

    fun downloadUpdate(release: ReleaseInfo) {
        _uiState.value = UpdateUiState.Downloading(0f, 0, release.apkSize)
        viewModelScope.launch {
            val result = ApkDownloader.downloadApk(
                url = release.apkUrl,
                destFile = apkFile,
                onProgress = { downloaded, total ->
                    if (total > 0) {
                        _uiState.value = UpdateUiState.Downloading(
                            progress = downloaded.toFloat() / total.toFloat(),
                            downloaded = downloaded,
                            total = total,
                        )
                    }
                },
            )
            result.fold(
                onSuccess = { file ->
                    _uiState.value = UpdateUiState.ReadyToInstall(file)
                },
                onFailure = { error ->
                    Log.e(TAG, "下载失败", error)
                    _uiState.value = UpdateUiState.Error(
                        error.message ?: "下载失败，请重试"
                    )
                },
            )
        }
    }

    fun installApk() {
        val state = _uiState.value
        if (state !is UpdateUiState.ReadyToInstall) return

        val context = getApplication<Application>()
        if (!ApkInstaller.canInstall(context)) {
            ApkInstaller.openInstallPermissionSettings(context)
            return
        }

        val intent = ApkInstaller.getInstallIntent(context, state.apkFile)
        context.startActivity(intent)
    }

    fun resetState() {
        _uiState.value = UpdateUiState.Idle
    }

    companion object {
        private const val TAG = "UpdateViewModel"
    }
}
