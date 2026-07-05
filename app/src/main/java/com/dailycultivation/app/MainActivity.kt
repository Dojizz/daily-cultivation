package com.dailycultivation.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailycultivation.app.ui.MainScreen
import com.dailycultivation.app.ui.theme.DailyCultivationTheme
import com.dailycultivation.app.viewmodel.HomeViewModel
import com.dailycultivation.app.viewmodel.JournalViewModel
import com.dailycultivation.app.viewmodel.PracticeViewModel
import com.dailycultivation.app.viewmodel.UpdateViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyCultivationTheme {
                val taskVM: HomeViewModel = viewModel()
                val practiceVM: PracticeViewModel = viewModel()
                val journalVM: JournalViewModel = viewModel()
                val updateVM: UpdateViewModel = viewModel()

                val activeTasks by taskVM.activeTasks.collectAsStateWithLifecycle()
                val expiredTasks by taskVM.expiredTasks.collectAsStateWithLifecycle()
                val todayState by practiceVM.todayState.collectAsStateWithLifecycle()
                val allPractices by practiceVM.allPractices.collectAsStateWithLifecycle()
                val todayJournal by journalVM.todayJournal.collectAsStateWithLifecycle()
                val allJournals by journalVM.allJournals.collectAsStateWithLifecycle()
                val updateState by updateVM.uiState.collectAsStateWithLifecycle()

                MainScreen(
                    activeTasks = activeTasks,
                    expiredTasks = expiredTasks,
                    onAddTask = taskVM::addTask,
                    onCompleteTask = taskVM::completeTask,
                    onRestartTask = taskVM::restartTask,
                    todayState = todayState,
                    allPractices = allPractices,
                    onCheckIn = { practiceVM.checkIn() },
                    onAddPractice = practiceVM::addPractice,
                    onEditPractice = practiceVM::updatePractice,
                    onToggleActive = practiceVM::toggleActive,
                    onDeletePractice = practiceVM::deletePractice,
                    todayJournal = todayJournal,
                    allJournals = allJournals,
                    onSaveJournal = journalVM::saveToday,
                    onUpdateJournal = journalVM::updateJournal,
                    onDeleteJournal = journalVM::deleteJournal,
                    updateState = updateState,
                    onCheckUpdate = updateVM::checkUpdate,
                    onDownloadUpdate = updateVM::downloadUpdate,
                    onInstallUpdate = updateVM::installApk,
                    onDismissUpdate = updateVM::resetState,
                    onRestartApp = { finishAffinity() },
                )
            }
        }
    }
}
