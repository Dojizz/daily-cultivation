package com.dailycultivation.app.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.PracticeEntity
import com.dailycultivation.app.data.entity.PracticeRecordEntity
import com.dailycultivation.app.data.entity.PracticeType
import com.dailycultivation.app.viewmodel.PracticeViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ShanghaiZone: ZoneId = ZoneId.systemDefault()
private val MonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy 年 M 月", Locale.CHINA)

@Composable
fun ReviewContent(
    state: PracticeViewModel.ReviewState,
    onChangeMonth: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val month = remember(state.monthStart) { state.monthStart.toYearMonth() }
    val habits = remember(state.practices) {
        state.practices.filter { it.type == PracticeType.HABIT && it.isActive }
    }
    var selectedPracticeId by rememberSaveable { mutableLongStateOf(0L) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    if (selectedPracticeId != 0L && habits.none { it.id == selectedPracticeId }) {
        selectedPracticeId = 0L
    }
    val selectedHabit = habits.firstOrNull { it.id == selectedPracticeId }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MonthHeader(month = month, onChangeMonth = onChangeMonth)

        if (habits.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedHabit == null,
                    onClick = { selectedPracticeId = 0L },
                    label = { Text("总览") },
                )
                habits.forEach { habit ->
                    FilterChip(
                        selected = habit.id == selectedPracticeId,
                        onClick = { selectedPracticeId = habit.id },
                        label = { Text(habit.name) },
                    )
                }
            }
        }

        if (selectedHabit == null) {
            OverviewSummary(habits = habits, records = state.records)
        } else {
            HabitSummary(habit = selectedHabit, records = state.records)
        }

        MonthCalendar(
            month = month,
            habits = habits,
            practices = state.practices,
            records = state.records,
            selectedHabit = selectedHabit,
            onDateClick = { selectedDate = it },
        )
    }

    selectedDate?.let { date ->
        DayDetailDialog(
            date = date,
            practices = state.practices,
            records = state.records.filter { it.date.toLocalDate() == date },
            onDismiss = { selectedDate = null },
        )
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onChangeMonth: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = { onChangeMonth(-1) }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
        }
        Text(month.format(MonthFormatter), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = { onChangeMonth(1) }) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
private fun OverviewSummary(habits: List<PracticeEntity>, records: List<PracticeRecordEntity>) {
    val habitRecords = records.filter { record -> habits.any { it.id == record.practiceId } }
    val practiceDays = habitRecords.map { it.date }.distinct().size
    val totalMinutes = habitRecords.sumOf { it.durationMinutes }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryValue("练习天数", "${practiceDays} 天")
            SummaryValue("总投入", "${totalMinutes} 分钟")
            SummaryValue("每日功课", "${habits.size} 项")
        }
    }
}

@Composable
private fun HabitSummary(habit: PracticeEntity, records: List<PracticeRecordEntity>) {
    val habitRecords = records.filter { it.practiceId == habit.id }
    val totalMinutes = habitRecords.sumOf { it.durationMinutes }
    val days = habitRecords.size
    val longestStreak = longestStreak(habitRecords.map { it.date.toLocalDate() }.toSet())
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryValue("总投入", "${totalMinutes} 分钟")
            SummaryValue("练习天数", "${days} 天")
            SummaryValue("连续最长", "${longestStreak} 天")
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    habits: List<PracticeEntity>,
    practices: List<PracticeEntity>,
    records: List<PracticeRecordEntity>,
    selectedHabit: PracticeEntity?,
    onDateClick: (LocalDate) -> Unit,
) {
    val recordsByDate = remember(records) { records.groupBy { it.date.toLocalDate() } }
    val practicesById = remember(practices) { practices.associateBy { it.id } }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val leadingDays = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
        val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
        val cells = List(leadingDays) { null } + days + List((7 - (leadingDays + days.size) % 7) % 7) { null }
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    if (date == null) Spacer(modifier = Modifier.weight(1f))
                    else CalendarDay(
                        modifier = Modifier.weight(1f),
                        date = date,
                        habits = habits,
                        records = recordsByDate[date].orEmpty(),
                        practicesById = practicesById,
                        selectedHabit = selectedHabit,
                        onClick = { onDateClick(date) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    modifier: Modifier,
    date: LocalDate,
    habits: List<PracticeEntity>,
    records: List<PracticeRecordEntity>,
    practicesById: Map<Long, PracticeEntity>,
    selectedHabit: PracticeEntity?,
    onClick: () -> Unit,
) {
    val habitRecords = records.filter { practicesById[it.practiceId]?.type == PracticeType.HABIT }
    val virtueDone = records.any { practicesById[it.practiceId]?.type == PracticeType.VIRTUE }
    val selectedRecord = selectedHabit?.let { habit -> records.firstOrNull { it.practiceId == habit.id } }
    val minutes = selectedRecord?.durationMinutes ?: 0
    val hasRecord = if (selectedHabit == null) habitRecords.isNotEmpty() || virtueDone else selectedRecord != null
    val color = when {
        !hasRecord -> Color.Transparent
        selectedHabit != null -> MaterialTheme.colorScheme.primary.copy(alpha = (0.16f + minutes.coerceAtMost(120) / 120f * 0.60f))
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f + habitRecords.size.coerceAtMost(3) * 0.12f)
    }
    Column(
        modifier = modifier.height(54.dp).clip(MaterialTheme.shapes.small)
            .background(color).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(2.dp))
        if (selectedHabit != null && selectedRecord != null) {
            Text(if (minutes > 0) "${minutes}m" else "✓", style = MaterialTheme.typography.labelSmall)
        } else if (selectedHabit == null && hasRecord) {
            Text(
                text = buildString {
                    if (habits.isNotEmpty()) append("${habitRecords.size}/${habits.size}")
                    if (virtueDone) append(" · ✓")
                },
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    practices: List<PracticeEntity>,
    records: List<PracticeRecordEntity>,
    onDismiss: () -> Unit,
) {
    val practicesById = remember(practices) { practices.associateBy { it.id } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(DateTimeFormatter.ofPattern("M 月 d 日", Locale.CHINA))) },
        text = {
            if (records.isEmpty()) Text("当天没有日课记录")
            else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                records.sortedBy { practicesById[it.practiceId]?.sortOrder }.forEach { record ->
                    val practice = practicesById[record.practiceId] ?: return@forEach
                    val value = if (practice.type == PracticeType.HABIT) "${record.durationMinutes} 分钟" else "已完成"
                    Text("${practice.name} · $value", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ShanghaiZone).toLocalDate()

private fun Long.toYearMonth(): YearMonth = Instant.ofEpochMilli(this).atZone(ShanghaiZone).toLocalDate().let(YearMonth::from)

private fun longestStreak(dates: Set<LocalDate>): Int {
    var longest = 0
    var current = 0
    dates.sorted().forEachIndexed { index, date ->
        current = if (index > 0 && date == dates.sorted()[index - 1].plusDays(1)) current + 1 else 1
        longest = maxOf(longest, current)
    }
    return longest
}
