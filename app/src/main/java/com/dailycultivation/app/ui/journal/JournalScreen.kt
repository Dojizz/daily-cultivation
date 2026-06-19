package com.dailycultivation.app.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.JournalEntity
import com.dailycultivation.app.ui.theme.Normal
import com.dailycultivation.app.ui.theme.Urgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalContent(
    todayJournal: JournalEntity?,
    allJournals: List<JournalEntity>,
    onSave: (String) -> Unit,
    onUpdate: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayDate = JournalEntity.todayDate()
    var todayContent by rememberSaveable { mutableStateOf(todayJournal?.content ?: "") }
    var hasEditedToday by rememberSaveable { mutableStateOf(false) }

    // 编辑过往日记的对话框状态
    var editingJournal by rememberSaveable { mutableStateOf<JournalEntity?>(null) }

    if (!hasEditedToday && todayJournal != null && todayContent != todayJournal.content) {
        todayContent = todayJournal.content
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 今天日记编辑 ──
        item {
            Text(
                text = formatDateHeader(todayDate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = todayContent,
                        onValueChange = {
                            todayContent = it
                            hasEditedToday = true
                        },
                        placeholder = { Text("今天做了什么、想了什么...") },
                        minLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onSave(todayContent)
                            hasEditedToday = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Normal),
                    ) {
                        Text(if (todayJournal != null) "更新日记" else "保存日记")
                    }
                }
            }
        }

        // ── 过往日记 ──
        val pastJournals = allJournals.filter { it.date != todayDate }
        if (pastJournals.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "过往日记 · ${pastJournals.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            items(pastJournals, key = { it.id }) { journal ->
                PastJournalCard(
                    journal = journal,
                    onClick = { editingJournal = journal },
                )
            }
        }
    }

    // ── 过往日记编辑对话框 ──
    if (editingJournal != null) {
        EditJournalDialog(
            journal = editingJournal!!,
            onDismiss = { editingJournal = null },
            onSave = { content ->
                onUpdate(editingJournal!!.id, content)
                editingJournal = null
            },
            onDelete = {
                onDelete(editingJournal!!.id)
                editingJournal = null
            },
        )
    }
}

@Composable
private fun PastJournalCard(
    journal: JournalEntity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatDateShort(journal.date),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = journal.content.ifBlank { "（空）" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 5,
            )
        }
    }
}

@Composable
private fun EditJournalDialog(
    journal: JournalEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var content by rememberSaveable { mutableStateOf(journal.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(formatDateShort(journal.date)) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(content) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("删除", color = Urgent)
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
    )
}

// ── 日期格式化 ──

private val headerFormat = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)
private val shortFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)

private fun formatDateHeader(timestamp: Long): String = headerFormat.format(Date(timestamp))
private fun formatDateShort(timestamp: Long): String = shortFormat.format(Date(timestamp))
