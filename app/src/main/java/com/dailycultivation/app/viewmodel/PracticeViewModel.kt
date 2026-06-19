package com.dailycultivation.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailycultivation.app.data.db.AppDatabase
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import com.dailycultivation.app.data.repository.PracticeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PracticeRepository(
        AppDatabase.getInstance(application).practiceDao()
    )

    /** 用于计算每日轮转的基准日 */
    private val rotationEpochMs: Long = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.JUNE)
        set(Calendar.DAY_OF_MONTH, 19)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val allPractices: StateFlow<List<PracticeEntity>> = repository.observeAllPractices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class TodayState(
        val practice: PracticeEntity? = null,
        val record: PracticeRecordEntity? = null,
        val isCheckedIn: Boolean = false,
        val note: String = "",
    )

    val todayState: StateFlow<TodayState> = combine(
        repository.observeAllPractices(),
        repository.observeTodayRecords(),
    ) { practices, records ->
        val active = practices.filter { it.isActive }.sortedBy { it.sortOrder }
        if (active.isEmpty()) {
            TodayState()
        } else {
            val todayStart = PracticeRepository.getTodayStartMs()
            val daysSince = TimeUnit.MILLISECONDS.toDays(todayStart - rotationEpochMs).toInt()
            val practice = active[daysSince % active.size]
            val record = records.firstOrNull { it.practiceId == practice.id }
            TodayState(
                practice = practice,
                record = record,
                isCheckedIn = record != null,
                note = record?.note ?: "",
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayState())

    fun checkIn(note: String = "") {
        viewModelScope.launch {
            val practice = todayState.value.practice ?: return@launch
            repository.checkIn(practice.id, note)
        }
    }

    fun addPractice(name: String, description: String = "") {
        viewModelScope.launch {
            repository.addPractice(name, description)
        }
    }

    fun updatePractice(practice: PracticeEntity) {
        viewModelScope.launch {
            repository.updatePractice(practice)
        }
    }

    fun deletePractice(id: Long) {
        viewModelScope.launch {
            repository.deletePractice(id)
        }
    }

    fun toggleActive(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            val practice = allPractices.value.find { it.id == id } ?: return@launch
            repository.updatePractice(practice.copy(isActive = isActive))
        }
    }
}
