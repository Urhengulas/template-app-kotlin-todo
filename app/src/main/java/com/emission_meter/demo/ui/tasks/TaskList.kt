package com.emission_meter.demo.ui.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.emission_meter.demo.data.MockRepository
import com.emission_meter.demo.data.SyncRepository
import com.emission_meter.demo.presentation.tasks.ItemContextualMenuViewModel
import com.emission_meter.demo.presentation.tasks.TaskViewModel
import com.emission_meter.demo.ui.theme.MyApplicationTheme

@Composable
fun TaskList(
    repository: SyncRepository,
    taskViewModel: TaskViewModel
) {
    Column {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        )
        {
            val taskList = taskViewModel.taskListState
            items(taskList.size) { index: Int ->
                TaskItem(
                    taskViewModel,
                    ItemContextualMenuViewModel(repository, taskViewModel),
                    taskList[index]
                )
                Divider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val tasks = (1..30).map { index ->
            MockRepository.getMockTask(index)
        }.toMutableStateList()

        MyApplicationTheme {
            TaskList(repository, TaskViewModel(repository, tasks))
        }
    }
}
