package com.dailycultivation.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.TaskEntity
import com.dailycultivation.app.data.repository.TaskWithDeadline
import com.dailycultivation.app.ui.task.AddTaskDialog
import com.dailycultivation.app.ui.task.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeTasks: List<TaskWithDeadline>,
    completedTasks: List<TaskEntity>,
    expiredTasks: List<TaskWithDeadline>,
    onAddTask: (String, String) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onRestartTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日课") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 活跃任务
            if (activeTasks.isNotEmpty()) {
                item {
                    SectionHeader(title = "进行中", count = activeTasks.size)
                }
                items(activeTasks, key = { it.task.id }) { item ->
                    TaskCard(
                        task = item.task,
                        remainingMs = item.remainingMs,
                        onComplete = { onCompleteTask(item.task.id) },
                        onRestart = { onRestartTask(item.task.id) },
                    )
                }
            }

            // 已过期
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

            // 空状态
            if (activeTasks.isEmpty() && expiredTasks.isEmpty()) {
                item {
                    EmptyState()
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc ->
                onAddTask(title, desc)
                showAddDialog = false
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
private fun EmptyState() {
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
            text = "点击右下角 + 添加一个任务\n72 小时内完成它",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
        )
    }
}
