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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.TaskEntity
import com.dailycultivation.app.data.repository.TaskWithDeadline
import com.dailycultivation.app.ui.task.TaskCard

@Composable
fun TaskContent(
    activeTasks: List<TaskWithDeadline>,
    completedTasks: List<TaskEntity>,
    expiredTasks: List<TaskWithDeadline>,
    cancelledTasks: List<TaskEntity>,
    onCompleteTask: (Long) -> Unit,
    onCancelTask: (Long) -> Unit,
    onRestartTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var deleteConfirmId by rememberSaveable { mutableLongStateOf(-1L) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 进行中 ──
        if (activeTasks.isNotEmpty()) {
            item { SectionHeader(title = "进行中", count = activeTasks.size) }
            items(activeTasks, key = { "active_${it.task.id}" }) { item ->
                TaskCard(
                    task = item.task,
                    remainingMs = item.remainingMs,
                    onComplete = { onCompleteTask(item.task.id) },
                    onCancel = { onCancelTask(item.task.id) },
                    onRestart = { onRestartTask(item.task.id) },
                    onDelete = { deleteConfirmId = item.task.id },
                )
            }
        }

        // ── 已完成 ──
        if (completedTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "已完成", count = completedTasks.size)
            }
            items(completedTasks, key = { "completed_${it.id}" }) { task ->
                TaskCard(
                    task = task,
                    remainingMs = 0,
                    isCompleted = true,
                    onComplete = {},
                    onCancel = {},
                    onRestart = {},
                    onDelete = { deleteConfirmId = task.id },
                )
            }
        }

        // ── 已终止 ──
        if (cancelledTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "已终止", count = cancelledTasks.size)
            }
            items(cancelledTasks, key = { "cancelled_${it.id}" }) { task ->
                TaskCard(
                    task = task,
                    remainingMs = 0,
                    isCancelled = true,
                    onComplete = {},
                    onCancel = {},
                    onRestart = { onRestartTask(task.id) },
                    onDelete = { deleteConfirmId = task.id },
                )
            }
        }

        // ── 已过期 ──
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
                    onCancel = {},
                    onRestart = { onRestartTask(item.task.id) },
                    onDelete = { deleteConfirmId = item.task.id },
                )
            }
        }

        if (activeTasks.isEmpty() && completedTasks.isEmpty() && expiredTasks.isEmpty() && cancelledTasks.isEmpty()) {
            item { TaskEmptyState() }
        }
    }

    // ── 删除确认对话框 ──
    if (deleteConfirmId > 0) {
        AlertDialog(
            onDismissRequest = { deleteConfirmId = -1 },
            title = { Text("确认删除") },
            text = { Text("删除后无法恢复，确定要删除这个任务吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTask(deleteConfirmId)
                    deleteConfirmId = -1
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = -1 }) {
                    Text("取消")
                }
            },
        )
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
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
