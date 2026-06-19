package com.dailycultivation.app.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.ui.theme.Normal
import com.dailycultivation.app.viewmodel.PracticeViewModel

@Composable
fun PracticeContent(
    todayState: PracticeViewModel.TodayState,
    allPractices: List<PracticeEntity>,
    onCheckIn: () -> Unit,
    onAddPractice: (String, String) -> Unit,
    onEditPractice: (PracticeEntity) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDeletePractice: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editingPractice by rememberSaveable { mutableStateOf<PracticeEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // —— 今日日课 ——
        item {
            Text(
                text = "今日日课",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item {
            if (todayState.practice != null) {
                TodayPracticeCard(
                    practice = todayState.practice,
                    isCheckedIn = todayState.isCheckedIn,
                    onCheckIn = onCheckIn,
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = "还没有日课\n点击 + 添加你的第一项目课",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
            }
        }

        // —— 日课列表 ——
        if (allPractices.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "全部日课 · ${allPractices.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            items(allPractices, key = { it.id }) { practice ->
                PracticeListItem(
                    practice = practice,
                    onClick = {
                        editingPractice = practice
                        showEditDialog = true
                    },
                    onToggleActive = { onToggleActive(practice.id, !practice.isActive) },
                )
            }
        }
    }

    // —— 编辑对话框 ——
    if (showEditDialog) {
        EditPracticeDialog(
            practice = editingPractice,
            onDismiss = { showEditDialog = false; editingPractice = null },
            onConfirm = { name, description ->
                if (editingPractice != null) {
                    onEditPractice(editingPractice!!.copy(name = name, description = description))
                } else {
                    onAddPractice(name, description)
                }
                showEditDialog = false
                editingPractice = null
            },
            onDelete = editingPractice?.let { practice ->
                { onDeletePractice(practice.id); showEditDialog = false; editingPractice = null }
            },
        )
    }
}

@Composable
private fun TodayPracticeCard(
    practice: PracticeEntity,
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCheckedIn)
                Normal.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = practice.name,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )
                if (isCheckedIn) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "已打卡",
                        tint = Normal,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            if (practice.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = practice.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCheckIn,
                enabled = !isCheckedIn,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Normal,
                    disabledContainerColor = Normal.copy(alpha = 0.3f),
                ),
            ) {
                Icon(
                    imageVector = if (isCheckedIn) Icons.Default.CheckCircle
                    else Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCheckedIn) "今日已打卡" else "打卡")
            }
        }
    }
}

@Composable
private fun PracticeListItem(
    practice: PracticeEntity,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (practice.isActive)
                MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = practice.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (practice.isActive)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
                if (practice.description.isNotBlank()) {
                    Text(
                        text = practice.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                    )
                }
            }

            if (!practice.isActive) {
                TextButton(onClick = onToggleActive) {
                    Text("启用", color = Normal)
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "编辑",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}
