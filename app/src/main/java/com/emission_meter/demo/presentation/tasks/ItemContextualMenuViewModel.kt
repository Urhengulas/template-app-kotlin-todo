package com.emission_meter.demo.presentation.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emission_meter.demo.data.SyncRepository
import com.emission_meter.demo.domain.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemContextualMenuViewModel constructor(
    private val repository: SyncRepository,
    private val taskViewModel: TaskViewModel
) : ViewModel() {

    private val _visible: MutableState<Boolean> = mutableStateOf(false)
    val visible: State<Boolean>
        get() = _visible

    fun open() {
        _visible.value = true
    }

    fun close() {
        _visible.value = false
    }

    fun deleteTask(task: Item) {
        CoroutineScope(Dispatchers.IO).launch {
            if (repository.isTaskMine(task)) {
                runCatching {
                    repository.deleteTask(task)
                }
            } else {
                taskViewModel.showPermissionsMessage()
            }
        }
        close()
    }
}
