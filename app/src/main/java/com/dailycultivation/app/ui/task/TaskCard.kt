package com.dailycultivation.app.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.DEADLINE_DURATION_MS
import com.dailycultivation.app.data.entity.TaskEntity
import com.dailycultivation.app.ui.theme.Expired
import com.dailycultivation.app.ui.theme.Normal
import com.dailycultivation.app.ui.theme.Primary
import com.dailycultivation.app.ui.theme.Urgent
import com.dailycultivation.app.ui.theme.Warning
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskCard(
    task: TaskEntity,
    remainingMs: Long,
    isExpired: Boolean = false,
    isCompleted: Boolean = false,
    isCancelled: Boolean = false,
    isLongTerm: Boolean = false,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onRestart: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 每秒更新一次倒计时（仅活跃短期任务）
    val isActive = !isExpired && !isCompleted && !isCancelled
    var tick by remember { mutableLongStateOf(remainingMs) }

    LaunchedEffect(remainingMs) {
        tick = remainingMs
        if (isActive && !isLongTerm && tick > 0) {
            while (tick > 0) {
                delay(1000L)
                tick -= 1000L
            }
        }
    }

    val progress = if (isExpired || isCompleted || isCancelled || isLongTerm) 1f
    else (1f - tick.toFloat() / DEADLINE_DURATION_MS).coerceIn(0f, 1f)

    val accentColor = when {
        isCompleted -> Normal
        isCancelled -> MaterialTheme.colorScheme.outline
        isExpired -> Expired
        isLongTerm -> Primary
        tick <= 6 * 60 * 60 * 1000L -> Urgent
        tick <= 24 * 60 * 60 * 1000L -> Warning
        else -> Normal
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Normal.copy(alpha = 0.06f)
                isCancelled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                isExpired -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                isLongTerm -> Primary.copy(alpha = 0.04f)
                else -> MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── 标题行：标题 + 操作按钮 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        isCancelled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        isExpired -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        isLongTerm -> Primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                )

                // 操作按钮
                Row {
                    if (!isCompleted && !isExpired && !isCancelled) {
                        IconButton(
                            onClick = onComplete,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Normal),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "完成")
                        }
                        IconButton(
                            onClick = onCancel,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            ),
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "终止")
                        }
                    }
                    if (isExpired || isCancelled) {
                        IconButton(
                            onClick = onRestart,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Warning),
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "重启")
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        ),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            }

            // ── 描述 ──
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 时间信息 ──
            TimeInfoRow(
                task = task,
                isCompleted = isCompleted,
                isExpired = isExpired,
                isCancelled = isCancelled,
                isLongTerm = isLongTerm,
                tick = tick,
            )

            // ── 进度条（仅短期活跃任务） ──
            if (!isCompleted && !isExpired && !isLongTerm) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.12f),
                )
            }
        }
    }
}

@Composable
private fun TimeInfoRow(
    task: TaskEntity,
    isCompleted: Boolean,
    isExpired: Boolean,
    isCancelled: Boolean,
    isLongTerm: Boolean,
    tick: Long,
) {
    Column {
        // 创建时间（所有状态都显示）
        Text(
            text = "创建: ${formatDateTime(task.createdAt)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        )

        when {
            isCompleted -> {
                val durationMs = (task.completedAt ?: System.currentTimeMillis()) - task.createdAt
                Text(
                    text = "完成: ${formatDateTime(task.completedAt ?: 0)} · 耗时 ${formatDuration(durationMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Normal,
                )
            }
            isCancelled -> {
                Text(
                    text = "终止: ${formatDateTime(task.cancelledAt ?: 0)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            isExpired -> {
                val expiredAt = task.createdAt + DEADLINE_DURATION_MS
                Text(
                    text = "超时: ${formatDateTime(expiredAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Expired,
                )
            }
            isLongTerm -> {
                val elapsedMs = System.currentTimeMillis() - task.createdAt
                Text(
                    text = "已持续 ${formatDuration(elapsedMs)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                )
            }
            else -> {
                Text(
                    text = formatRemaining(tick),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        tick <= 6 * 60 * 60 * 1000L -> Urgent
                        tick <= 24 * 60 * 60 * 1000L -> Warning
                        else -> Normal
                    },
                )
            }
        }
    }
}

// ── 格式化工具 ──

private val dateTimeFormat = SimpleDateFormat("M月d日 HH:mm", Locale.CHINESE)

private fun formatDateTime(ms: Long): String {
    if (ms <= 0) return "--"
    return dateTimeFormat.format(Date(ms))
}

fun formatRemaining(ms: Long): String {
    if (ms <= 0) return "已到期"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "剩余 ${hours}h ${minutes}m"
    else if (minutes > 0) "剩余 ${minutes}m"
    else "剩余 ${totalSeconds}s"
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "--"
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m"
    else "${minutes}m"
}
