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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeType
import com.dailycultivation.app.ui.theme.Normal
import com.dailycultivation.app.viewmodel.PracticeViewModel

@Composable
fun PracticeContent(
    habits: List<PracticeViewModel.HabitState>,
    todayState: PracticeViewModel.TodayState,
    allPractices: List<PracticeEntity>,
    onCheckInHabit: (Long, Int) -> Unit,
    onCheckInVirtue: () -> Unit,
    onAddPractice: (String, String, PracticeType) -> Unit,
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
        // ── 每日习惯 ──
        item {
            Text(
                text = "每日功课",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        if (habits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = "还没有功课\n点击 + 添加每日功课",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }
            }
        } else {
            items(habits, key = { "habit_${it.practice.id}" }) { habit ->
                HabitCard(
                    state = habit,
                    onCheckIn = { minutes -> onCheckInHabit(habit.practice.id, minutes) },
                )
            }
        }

        // ── 今日特制 ──
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "今日特质",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item {
            if (todayState.practice != null) {
                VirtueCard(
                    practice = todayState.practice,
                    isCheckedIn = todayState.isCheckedIn,
                    onCheckIn = onCheckInVirtue,
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = "还没有特质日课\n点击 + 添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }
            }
        }

        // ── 全部日课（管理） ──
        if (allPractices.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "全部日课 · ${allPractices.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            items(allPractices, key = { "practice_${it.id}" }) { practice ->
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

    // ── 编辑对话框 ──
    if (showEditDialog) {
        EditPracticeDialog(
            practice = editingPractice,
            onDismiss = { showEditDialog = false; editingPractice = null },
            onConfirm = { name, description, type ->
                if (editingPractice != null) {
                    onEditPractice(
                        editingPractice!!.copy(name = name, description = description, type = type)
                    )
                } else {
                    onAddPractice(name, description, type)
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

// ── 习惯卡片 ──

@Composable
private fun HabitCard(
    state: PracticeViewModel.HabitState,
    onCheckIn: (Int) -> Unit,
) {
    var durationText by rememberSaveable { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isCheckedIn)
                Normal.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.practice.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (state.practice.description.isNotBlank()) {
                        Text(
                            text = state.practice.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
                if (state.isCheckedIn) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "已打卡",
                        tint = Normal,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isCheckedIn) {
                Text(
                    text = "今日已投入 ${state.durationMinutes} 分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Normal,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                                durationText = newValue
                            }
                        },
                        placeholder = { Text("分钟") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val minutes = durationText.toIntOrNull() ?: 0
                            if (minutes > 0) {
                                onCheckIn(minutes)
                                durationText = ""
                            }
                        },
                        enabled = (durationText.toIntOrNull() ?: 0) > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Normal),
                    ) {
                        Icon(
                            Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("打卡")
                    }
                }
            }
        }
    }
}

// ── 特制卡片 ──

@Composable
private fun VirtueCard(
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
                        Icons.Default.CheckCircle,
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
                Text(if (isCheckedIn) "今日已关注" else "打卡关注")
            }
        }
    }
}

// ── 列表项 ──

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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = practice.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (practice.isActive)
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                    val typeLabel = when (practice.type) {
                        PracticeType.HABIT -> "功课"
                        PracticeType.VIRTUE -> "特质"
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
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
                Icons.Default.ChevronRight,
                contentDescription = "编辑",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}
