package com.dailycultivation.app.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dailycultivation.app.data.entity.JournalEntity
import com.dailycultivation.app.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalContent(
    todayJournal: JournalEntity?,
    allJournals: List<JournalEntity>,
    onJournalClick: (JournalEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayDate = JournalEntity.todayDate()

    if (allJournals.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "暂无日记",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "点击右下角 + 写下第一篇",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(allJournals, key = { it.id }) { journal ->
                JournalCard(
                    journal = journal,
                    isToday = journal.date == todayDate,
                    onClick = { onJournalClick(journal) },
                )
            }
        }
    }
}

@Composable
private fun JournalCard(
    journal: JournalEntity,
    isToday: Boolean,
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
            Row {
                Text(
                    text = formatDate(journal.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (isToday) {
                    Text(
                        text = " 今天",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = journal.content.ifBlank { "（点击编辑）" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (journal.content.isBlank())
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)

private fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
