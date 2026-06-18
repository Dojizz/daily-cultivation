package com.dailycultivation.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailycultivation.app.ui.home.HomeScreen
import com.dailycultivation.app.ui.theme.DailyCultivationTheme
import com.dailycultivation.app.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyCultivationTheme {
                val viewModel: HomeViewModel = viewModel()
                val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
                val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()
                val expiredTasks by viewModel.expiredTasks.collectAsStateWithLifecycle()

                HomeScreen(
                    activeTasks = activeTasks,
                    completedTasks = completedTasks,
                    expiredTasks = expiredTasks,
                    onAddTask = viewModel::addTask,
                    onCompleteTask = viewModel::completeTask,
                    onRestartTask = viewModel::restartTask,
                    onDeleteTask = viewModel::deleteTask,
                )
            }
        }
    }
}
