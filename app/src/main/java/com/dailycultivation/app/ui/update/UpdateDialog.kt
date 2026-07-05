package com.dailycultivation.app.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.network.ReleaseInfo
import com.dailycultivation.app.viewmodel.UpdateUiState
import com.dailycultivation.app.R

@Composable
fun UpdateDialog(
    state: UpdateUiState,
    onDownload: (ReleaseInfo) -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is UpdateUiState.Checking -> {
            CheckingDialog(onDismiss = onDismiss)
        }
        is UpdateUiState.UpToDate -> {
            UpToDateDialog(onDismiss = onDismiss)
        }
        is UpdateUiState.Available -> {
            AvailableDialog(
                release = state.release,
                onDownload = { onDownload(state.release) },
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.Downloading -> {
            DownloadingDialog(
                progress = state.progress,
                downloaded = state.downloaded,
                total = state.total,
            )
        }
        is UpdateUiState.ReadyToInstall -> {
            ReadyToInstallDialog(
                onInstall = onInstall,
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.Error -> {
            ErrorDialog(
                message = state.message,
                onRetry = onRetry,
                onDismiss = onDismiss,
            )
        }
        is UpdateUiState.Idle -> {
            // 不显示任何对话框
        }
    }
}

@Composable
private fun CheckingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_checking_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.update_checking))
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_cancel))
            }
        },
    )
}

@Composable
private fun UpToDateDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_up_to_date_title)) },
        text = { Text(stringResource(R.string.update_up_to_date)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_ok))
            }
        },
    )
}

@Composable
private fun AvailableDialog(
    release: ReleaseInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.update_new_version_title))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "v${release.versionName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (release.body.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.update_changelog_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = release.body.take(500),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = stringResource(
                        R.string.update_file_size,
                        formatFileSize(release.apkSize)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = onDownload) {
                Text(stringResource(R.string.update_download))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_later))
            }
        },
    )
}

@Composable
private fun DownloadingDialog(
    progress: Float,
    downloaded: Long,
    total: Long,
) {
    AlertDialog(
        onDismissRequest = { /* 下载中不允许关闭 */ },
        title = { Text(stringResource(R.string.update_downloading_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (total > 0) {
                        "${(progress * 100).toInt()}%  (${formatFileSize(downloaded)} / ${formatFileSize(total)})"
                    } else {
                        "${formatFileSize(downloaded)} 已下载"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}

@Composable
private fun ReadyToInstallDialog(
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_ready_title)) },
        text = { Text(stringResource(R.string.update_ready_message)) },
        confirmButton = {
            Button(onClick = onInstall) {
                Text(stringResource(R.string.update_install_now))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_later))
            }
        },
    )
}

@Composable
private fun ErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_error_title)) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.update_retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_cancel))
            }
        },
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
