package com.dailycultivation.app.ui.backup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.BuildConfig
import com.dailycultivation.app.data.db.BackupInfo
import com.dailycultivation.app.data.db.DatabaseBackupManager

/**
 * 备份选择对话框。列出所有可用备份，用户选择后恢复。
 */
@Composable
fun BackupListDialog(
    onDismiss: () -> Unit,
    onRestored: () -> Unit,
) {
    val context = LocalContext.current
    val backups = remember { DatabaseBackupManager.listBackups(context) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultSuccess by remember { mutableStateOf(false) }

    if (backups.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("恢复备份") },
            text = { Text("暂无可用备份。备份会在每次启动应用时自动创建。") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("知道了") }
            },
        )
        return
    }

    // ── 备份列表 ──────────────────────────────────────────────
    if (!showConfirmDialog && !showResultDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("选择备份") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(backups) { backup ->
                        BackupRow(
                            backup = backup,
                            isSelected = selectedIndex == backups.indexOf(backup),
                            onClick = { selectedIndex = backups.indexOf(backup) },
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = selectedIndex != null,
                ) {
                    Text("恢复选中备份")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("取消") }
            },
        )
        return
    }

    // ── 确认对话框 ────────────────────────────────────────────
    if (showConfirmDialog && !showResultDialog) {
        val selectedBackup = backups[selectedIndex!!]
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("确认恢复") },
            text = {
                Column {
                    Text("将恢复到以下备份：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedBackup.displayName,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "数据库大小: ${formatFileSize(selectedBackup.dbSize)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedBackup.metadata?.appVersion != null &&
                        selectedBackup.metadata.appVersion != BuildConfig.VERSION_NAME) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠ 备份来自 v${selectedBackup.metadata.appVersion}，当前为 v${BuildConfig.VERSION_NAME}。" +
                                    "如果数据库结构有变更，恢复可能导致 App 崩溃。",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠ 当前数据将被覆盖，此操作不可撤销。",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        resultSuccess = DatabaseBackupManager.restoreFromBackup(
                            context,
                            selectedBackup!!.folderName,
                        )
                        showConfirmDialog = false
                        showResultDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("确认恢复")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("取消")
                }
            },
        )
        return
    }

    // ── 结果对话框 ────────────────────────────────────────────
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                if (resultSuccess) onRestored()
                else onDismiss()
            },
            title = { Text(if (resultSuccess) "恢复成功" else "恢复失败") },
            text = {
                Text(
                    if (resultSuccess) "数据已恢复，请重启应用以加载恢复后的数据。"
                    else "备份文件可能已损坏，请尝试其他备份。"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    if (resultSuccess) onRestored()
                    else onDismiss()
                }) {
                    Text("知道了")
                }
            },
        )
    }
}

@Composable
private fun BackupRow(
    backup: BackupInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = backup.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                )
                backup.metadata?.let { meta ->
                    Spacer(modifier = Modifier.width(8.dp))
                    val isDifferent = meta.appVersion != BuildConfig.VERSION_NAME
                    Text(
                        text = "v${meta.appVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDifferent)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = formatFileSize(backup.dbSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
    }
}
