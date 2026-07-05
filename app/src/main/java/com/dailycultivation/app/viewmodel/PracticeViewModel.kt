package com.dailycultivation.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailycultivation.app.data.db.AppDatabase
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import com.dailycultivation.app.data.entity.PracticeType
import com.dailycultivation.app.data.repository.PracticeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PracticeRepository(
        AppDatabase.getInstance(application).practiceDao()
    )

    private val rotationEpochMs: Long = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.JUNE)
        set(Calendar.DAY_OF_MONTH, 19)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** 今日记录（用于 combine） */
    private val todayRecords = repository.observeTodayRecords()

    // ── 习惯 ──

    data class HabitState(
        val practice: PracticeEntity,
        val record: PracticeRecordEntity? = null,
        val isCheckedIn: Boolean = false,
        val durationMinutes: Int = 0,
    )

    val habits: StateFlow<List<HabitState>> = combine(
        repository.observeAllHabits(),
        todayRecords,
    ) { allHabits, records ->
        Log.d(TAG, "habits combine: allHabits.size=${allHabits.size}, records.size=${records.size}")
        allHabits.map { habit ->
            val record = records.firstOrNull { it.practiceId == habit.id }
            HabitState(
                practice = habit,
                record = record,
                isCheckedIn = record != null,
                durationMinutes = record?.durationMinutes ?: 0,
            )
        }
    }.catch { e ->
        Log.e(TAG, "habits Flow 崩溃", e)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── 特制 ──

    data class TodayState(
        val practice: PracticeEntity? = null,
        val record: PracticeRecordEntity? = null,
        val isCheckedIn: Boolean = false,
    )

    val todayState: StateFlow<TodayState> = combine(
        repository.observeAllVirtues(),
        todayRecords,
    ) { virtues, records ->
        Log.d(TAG, "todayState combine: virtues.size=${virtues.size}, records.size=${records.size}")
        val active = virtues.sortedBy { it.sortOrder }
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
            )
        }
    }.catch { e ->
        Log.e(TAG, "todayState Flow 崩溃", e)
        emit(TodayState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayState())

    val allPractices: StateFlow<List<PracticeEntity>> = repository.observeAllPractices()
        .catch { e ->
            Log.e(TAG, "allPractices Flow 崩溃", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        private const val TAG = "PracticeViewModel"
    }

    // ── 操作 ──

    /** 习惯打卡 */
    fun checkInHabit(practiceId: Long, durationMinutes: Int) {
        viewModelScope.launch {
            repository.checkIn(practiceId, durationMinutes = durationMinutes)
        }
    }

    /** 特制打卡 */
    fun checkInVirtue() {
        viewModelScope.launch {
            val practice = todayState.value.practice ?: return@launch
            repository.checkIn(practice.id)
        }
    }

    fun addPractice(name: String, description: String = "", type: PracticeType = PracticeType.VIRTUE) {
        viewModelScope.launch {
            repository.addPractice(name, description, type)
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
