package com.dailycultivation.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dailycultivation.app.data.entity.JournalEntity
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeType
import com.dailycultivation.app.data.network.ReleaseInfo
import com.dailycultivation.app.data.repository.TaskWithDeadline
import com.dailycultivation.app.ui.backup.BackupListDialog
import com.dailycultivation.app.ui.home.TaskContent
import com.dailycultivation.app.ui.journal.JournalContent
import com.dailycultivation.app.ui.journal.JournalEditorScreen
import com.dailycultivation.app.ui.practice.PracticeContent
import com.dailycultivation.app.ui.theme.Primary
import com.dailycultivation.app.ui.theme.Tertiary
import com.dailycultivation.app.ui.update.UpdateDialog
import com.dailycultivation.app.viewmodel.PracticeViewModel
import com.dailycultivation.app.viewmodel.UpdateUiState

enum class Tab(val label: String) {
    TASKS("任务"),
    PRACTICE("日课"),
    JOURNAL("日记"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    // 任务
    activeShortTasks: List<TaskWithDeadline>,
    activeLongTasks: List<TaskWithDeadline>,
    completedTasks: List<com.dailycultivation.app.data.entity.TaskEntity>,
    expiredTasks: List<TaskWithDeadline>,
    cancelledTasks: List<com.dailycultivation.app.data.entity.TaskEntity>,
    onAddTask: (String, String, com.dailycultivation.app.data.entity.TaskType) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onCancelTask: (Long) -> Unit,
    onRestartTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    // 日课
    todayState: PracticeViewModel.TodayState,
    habits: List<PracticeViewModel.HabitState>,
    allPractices: List<PracticeEntity>,
    onCheckInVirtue: () -> Unit,
    onCheckInHabit: (Long, Int) -> Unit,
    onAddPractice: (String, String, PracticeType) -> Unit,
    onEditPractice: (PracticeEntity) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDeletePractice: (Long) -> Unit,
    // 日记
    todayJournal: JournalEntity?,
    allJournals: List<JournalEntity>,
    onSaveJournal: (String) -> Unit,
    onUpdateJournal: (Long, String) -> Unit,
    onDeleteJournal: (Long) -> Unit,
    // 更新
    updateState: UpdateUiState,
    onCheckUpdate: () -> Unit,
    onDownloadUpdate: (ReleaseInfo) -> Unit,
    onInstallUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onRestartApp: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettingsMenu by rememberSaveable { mutableStateOf(false) }
    var showBackupDialog by rememberSaveable { mutableStateOf(false) }
    var editingJournal by rememberSaveable { mutableStateOf<JournalEntity?>(null) }

    // 折叠状态：跨重启保持
    val prefs = LocalContext.current.getSharedPreferences("task_sections", android.content.Context.MODE_PRIVATE)
    @Composable fun prefBool(key: String) = remember { mutableStateOf(prefs.getBoolean(key, true)) }
    var showShort by prefBool("showShort")
    var showLong by prefBool("showLong")
    var showCompleted by prefBool("showCompleted")
    var showCancelled by prefBool("showCancelled")
    var showExpired by prefBool("showExpired")
    SideEffect {
        prefs.edit()
            .putBoolean("showShort", showShort)
            .putBoolean("showLong", showLong)
            .putBoolean("showCompleted", showCompleted)
            .putBoolean("showCancelled", showCancelled)
            .putBoolean("showExpired", showExpired)
            .apply()
    }

    val tab = Tab.entries[selectedTab]

    // 日记编辑器全屏模式
    if (tab == Tab.JOURNAL && editingJournal != null) {
        val journal = editingJournal!!
        JournalEditorScreen(
            journal = journal,
            isNew = journal.id == 0L,
            onBack = { content ->
                if (journal.id == 0L) onSaveJournal(content)
                else onUpdateJournal(journal.id, content)
                editingJournal = null
            },
            onDelete = {
                onDeleteJournal(journal.id)
                editingJournal = null
            },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tab.label) },
                actions = {
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "设置",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("检查更新") },
                                onClick = {
                                    showSettingsMenu = false
                                    onCheckUpdate()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("恢复备份") },
                                onClick = {
                                    showSettingsMenu = false
                                    showBackupDialog = true
                                },
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
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    label = { Text("任务") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f),
                    ),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.SelfImprovement, contentDescription = null) },
                    label = { Text("日课") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f),
                    ),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("日记") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f),
                    ),
                )
            }
        },
        floatingActionButton = {
            when (tab) {
                Tab.TASKS -> AddTaskFab { title, desc, type -> onAddTask(title, desc, type) }
                Tab.PRACTICE -> AddPracticeFab { name, desc, type -> onAddPractice(name, desc, type) }
                Tab.JOURNAL -> AddJournalFab {
                    editingJournal = todayJournal
                        ?: JournalEntity(date = JournalEntity.todayDate())
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                Tab.TASKS -> TaskContent(
                    activeShortTasks = activeShortTasks,
                    activeLongTasks = activeLongTasks,
                    completedTasks = completedTasks,
                    expiredTasks = expiredTasks,
                    cancelledTasks = cancelledTasks,
                    onCompleteTask = onCompleteTask,
                    onCancelTask = onCancelTask,
                    onRestartTask = onRestartTask,
                    onDeleteTask = onDeleteTask,
                    showShort = showShort, onToggleShort = { showShort = !showShort },
                    showLong = showLong, onToggleLong = { showLong = !showLong },
                    showCompleted = showCompleted, onToggleCompleted = { showCompleted = !showCompleted },
                    showCancelled = showCancelled, onToggleCancelled = { showCancelled = !showCancelled },
                    showExpired = showExpired, onToggleExpired = { showExpired = !showExpired },
                )
                Tab.PRACTICE -> PracticeContent(
                    habits = habits,
                    todayState = todayState,
                    allPractices = allPractices,
                    onCheckInHabit = onCheckInHabit,
                    onCheckInVirtue = onCheckInVirtue,
                    onAddPractice = onAddPractice,
                    onEditPractice = onEditPractice,
                    onToggleActive = onToggleActive,
                    onDeletePractice = onDeletePractice,
                )
                Tab.JOURNAL -> JournalContent(
                    todayJournal = todayJournal,
                    allJournals = allJournals,
                    onJournalClick = { editingJournal = it },
                )
            }
        }

        if (updateState !is UpdateUiState.Idle) {
            UpdateDialog(
                state = updateState,
                onDownload = onDownloadUpdate,
                onInstall = onInstallUpdate,
                onDismiss = onDismissUpdate,
                onRetry = onCheckUpdate,
            )
        }

            if (showBackupDialog) {
            BackupListDialog(
                onDismiss = { showBackupDialog = false },
                onRestored = {
                    showBackupDialog = false
                    onRestartApp()
                },
            )
        }
    }
}

@Composable
private fun AddJournalFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ) {
        Icon(Icons.Default.Add, contentDescription = "写日记")
    }
}

@Composable
private fun AddTaskFab(onAddTask: (String, String, com.dailycultivation.app.data.entity.TaskType) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = Tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ) {
        Icon(Icons.Default.Add, contentDescription = "添加任务")
    }

    if (showDialog) {
        com.dailycultivation.app.ui.task.AddTaskDialog(
            onDismiss = { showDialog = false },
            onConfirm = { title, desc, type ->
                onAddTask(title, desc, type)
                showDialog = false
            },
        )
    }
}

@Composable
private fun AddPracticeFab(onAddPractice: (String, String, PracticeType) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = Tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ) {
        Icon(Icons.Default.Add, contentDescription = "添加日课")
    }

    if (showDialog) {
        com.dailycultivation.app.ui.practice.EditPracticeDialog(
            practice = null,
            onDismiss = { showDialog = false },
            onConfirm = { name, desc, type ->
                onAddPractice(name, desc, type)
                showDialog = false
            },
        )
    }
}
