package com.dailycultivation.app.ui.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycultivation.app.data.entity.JournalEntity
import com.dailycultivation.app.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    journal: JournalEntity,
    isNew: Boolean,
    onBack: (content: String) -> Unit,
    onDelete: () -> Unit,
) {
    var content by rememberSaveable { mutableStateOf(journal.content) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun exit() {
        focusManager.clearFocus()
        onBack(content)
    }

    // 系统返回键 → 自动保存
    BackHandler { exit() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(formatFullDate(journal.date))
                },
                navigationIcon = {
                    IconButton(onClick = { exit() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                actions = {
                    if (!isNew) {
                        TextButton(onClick = onDelete) {
                            Text(
                                "删除",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        BasicTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .focusRequester(focusRequester)
                .imePadding(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(Primary),
            decorationBox = { innerTextField ->
                if (content.isEmpty()) {
                    Text(
                        text = "写点什么...",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        ),
                    )
                }
                innerTextField()
            },
        )
    }
}

private val fullDateFormat = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)

private fun formatFullDate(timestamp: Long): String = fullDateFormat.format(Date(timestamp))
