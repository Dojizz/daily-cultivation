package com.dailycultivation.app.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.dailycultivation.app.ui.theme.Urgent
import com.dailycultivation.app.ui.theme.Warning
import kotlinx.coroutines.delay

@Composable
fun TaskCard(
    task: TaskEntity,
    remainingMs: Long,
    isExpired: Boolean = false,
    onComplete: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 每秒更新一次倒计时
    var tick by remember { mutableLongStateOf(remainingMs) }

    LaunchedEffect(remainingMs) {
        tick = remainingMs
        if (!isExpired && tick > 0) {
            while (tick > 0) {
                delay(1000L)
                tick -= 1000L
            }
        }
    }

    val progress = if (isExpired) 1f else (1f - tick.toFloat() / DEADLINE_DURATION_MS).coerceIn(0f, 1f)
    val accentColor = when {
        isExpired -> Expired
        tick <= 6 * 60 * 60 * 1000L -> Urgent      // < 6h
        tick <= 24 * 60 * 60 * 1000L -> Warning     // < 24h
        else -> Normal
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isExpired)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                if (!isExpired) {
                    IconButton(
                        onClick = onComplete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Normal,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "完成",
                        )
                    }
                } else {
                    IconButton(
                        onClick = onRestart,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Warning,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重启",
                        )
                    }
                }
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // 剩余时间
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isExpired) "已过期" else formatRemaining(tick),
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                )
                if (!isExpired) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }

            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.12f),
            )
        }
    }
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
