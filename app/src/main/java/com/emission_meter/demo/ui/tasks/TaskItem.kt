package com.emission_meter.demo.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emission_meter.demo.R
import com.emission_meter.demo.data.MockRepository
import com.emission_meter.demo.domain.Item
import com.emission_meter.demo.presentation.tasks.ItemContextualMenuViewModel
import com.emission_meter.demo.presentation.tasks.TaskViewModel
import com.emission_meter.demo.ui.theme.Blue
import com.emission_meter.demo.ui.theme.MyApplicationTheme

@Composable
fun TaskItem(
    taskViewModel: TaskViewModel,
    itemContextualMenuViewModel: ItemContextualMenuViewModel,
    task: Item
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Guard against modifying some else's task - sync error callback would catch it though
        Checkbox(
            checked = task.isComplete,
            onCheckedChange = {
                if (taskViewModel.isTaskMine(task)) {
                    taskViewModel.toggleIsComplete(task)
                } else {
                    taskViewModel.showPermissionsMessage()
                }
            }
        )
        Column {
            Text(
                text = task.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Blue,
                fontWeight = FontWeight.Bold
            )
            // Ownership text visible only if task is mine
            if (taskViewModel.isTaskMine(task)) {
                Text(
                    text = stringResource(R.string.mine),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Delete icon
//        if (repository.isTaskMine(task)) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            ItemContextualMenu(itemContextualMenuViewModel, task)
        }
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val taskViewModel = TaskViewModel(repository)
        TaskItem(
            taskViewModel,
            ItemContextualMenuViewModel(repository, taskViewModel),
            MockRepository.getMockTask(42)
        )
    }
}
