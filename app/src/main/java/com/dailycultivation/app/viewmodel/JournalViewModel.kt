package com.dailycultivation.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailycultivation.app.data.db.AppDatabase
import com.dailycultivation.app.data.entity.JournalEntity
import com.dailycultivation.app.data.repository.JournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JournalRepository(
        AppDatabase.getInstance(application).journalDao()
    )

    /** 今天的日记（3am 前算昨天） */
    val todayJournal: StateFlow<JournalEntity?> = repository.observeToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 全部日记列表（按日期倒序） */
    val allJournals: StateFlow<List<JournalEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveToday(content: String) {
        viewModelScope.launch {
            repository.saveToday(content)
        }
    }

    fun updateJournal(id: Long, content: String) {
        viewModelScope.launch {
            repository.updateContent(id, content)
        }
    }

    fun deleteJournal(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
