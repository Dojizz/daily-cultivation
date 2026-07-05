package com.dailycultivation.app.ui.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeType
import com.dailycultivation.app.ui.theme.Urgent

@Composable
fun EditPracticeDialog(
    practice: PracticeEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, type: PracticeType) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val isNew = practice == null
    var name by remember { mutableStateOf(practice?.name ?: "") }
    var description by remember { mutableStateOf(practice?.description ?: "") }
    var selectedType by remember { mutableStateOf(practice?.type ?: PracticeType.VIRTUE) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isNew) "添加日课" else "编辑日课")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("名称") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("名称不能为空") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("说明（可选）") },
                    placeholder = { Text("这项日课的具体含义是什么？") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "类型",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == PracticeType.HABIT,
                        onClick = { selectedType = PracticeType.HABIT },
                    )
                    Text(
                        text = "习惯",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                    RadioButton(
                        selected = selectedType == PracticeType.VIRTUE,
                        onClick = { selectedType = PracticeType.VIRTUE },
                    )
                    Text(
                        text = "特制",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = if (selectedType == PracticeType.HABIT)
                        "习惯每天都会出现，需要记录投入时间"
                    else "特制每天轮转，关注打卡即可",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(name.trim(), description.trim(), selectedType)
                    }
                }
            ) {
                Text(if (isNew) "添加" else "保存")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("删除", color = Urgent)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
    )
}
