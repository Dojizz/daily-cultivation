package com.dailycultivation.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.repository.TaskWithDeadline
import com.dailycultivation.app.ui.task.TaskCard

/**
 * 72h 任务列表内容（不含 Scaffold，由父级 MainScreen 提供 chrome 和 FAB）
 */
@Composable
fun TaskContent(
    activeTasks: List<TaskWithDeadline>,
    expiredTasks: List<TaskWithDeadline>,
    onCompleteTask: (Long) -> Unit,
    onRestartTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (activeTasks.isNotEmpty()) {
            item { SectionHeader(title = "进行中", count = activeTasks.size) }
            items(activeTasks, key = { it.task.id }) { item ->
                TaskCard(
                    task = item.task,
                    remainingMs = item.remainingMs,
                    onComplete = { onCompleteTask(item.task.id) },
                    onRestart = { onRestartTask(item.task.id) },
                )
            }
        }

        if (expiredTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "已过期", count = expiredTasks.size)
            }
            items(expiredTasks, key = { "expired_${it.task.id}" }) { item ->
                TaskCard(
                    task = item.task,
                    remainingMs = item.remainingMs,
                    isExpired = true,
                    onComplete = { onCompleteTask(item.task.id) },
                    onRestart = { onRestartTask(item.task.id) },
                )
            }
        }

        if (activeTasks.isEmpty() && expiredTasks.isEmpty()) {
            item { TaskEmptyState() }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Text(
        text = "$title · $count",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun TaskEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "还没有任务",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击 + 添加一个任务\n72 小时内完成它",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
        )
    }
}
