@file:OptIn(ExperimentalMaterial3Api::class)

package com.emission_meter.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.emission_meter.demo.data.MockRepository
import com.emission_meter.demo.data.RealmSyncRepository
import com.emission_meter.demo.data.SyncRepository
import com.emission_meter.demo.presentation.tasks.AddItemEvent
import com.emission_meter.demo.presentation.tasks.AddItemViewModel
import com.emission_meter.demo.presentation.tasks.SubscriptionTypeEvent
import com.emission_meter.demo.presentation.tasks.SubscriptionTypeViewModel
import com.emission_meter.demo.presentation.tasks.TaskViewModel
import com.emission_meter.demo.presentation.tasks.ToolbarEvent
import com.emission_meter.demo.presentation.tasks.ToolbarViewModel
import com.emission_meter.demo.ui.tasks.AddItemPrompt
import com.emission_meter.demo.ui.tasks.ShowMyOwnTasks
import com.emission_meter.demo.ui.tasks.TaskAppToolbar
import com.emission_meter.demo.ui.tasks.TaskList
import com.emission_meter.demo.ui.theme.MyApplicationTheme
import com.emission_meter.demo.ui.theme.Purple200
import kotlinx.coroutines.launch

class ComposeItemActivity : ComponentActivity() {

    private val repository = RealmSyncRepository { _, error ->
        // Sync errors come from a background thread so route the Toast through the UI thread
        lifecycleScope.launch {
            // Catch write permission errors and notify user. This is just a 2nd line of defense
            // since we prevent users from modifying someone else's tasks
            // TODO the SDK does not have an enum for this type of error yet so make sure to update this once it has been added
            if (error.message?.contains("CompensatingWrite") == true) {
                Toast.makeText(
                    this@ComposeItemActivity,
                    getString(R.string.permissions_error),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private val toolbarViewModel: ToolbarViewModel by viewModels {
        ToolbarViewModel.factory(repository, this)
    }
    private val addItemViewModel: AddItemViewModel by viewModels {
        AddItemViewModel.factory(repository, this)
    }
    private val subscriptionTypeViewModel: SubscriptionTypeViewModel by viewModels {
        SubscriptionTypeViewModel.factory(repository, this)
    }
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.factory(repository, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            taskViewModel.event
                .collect {
                    Log.i(
                        TAG(),
                        "Tried to modify or remove a task that doesn't belong to the current user."
                    )
                    Toast.makeText(
                        this@ComposeItemActivity,
                        getString(R.string.permissions_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        lifecycleScope.launch {
            toolbarViewModel.toolbarEvent
                .collect { toolbarEvent ->
                    when (toolbarEvent) {
                        ToolbarEvent.LogOut -> {
                            startActivity(
                                Intent(
                                    this@ComposeItemActivity,
                                    ComposeLoginActivity::class.java
                                )
                            )
                            finish()
                        }

                        is ToolbarEvent.Info ->
                            Log.e(TAG(), toolbarEvent.message)

                        is ToolbarEvent.Error ->
                            Log.e(
                                TAG(),
                                "${toolbarEvent.message}: ${toolbarEvent.throwable.message}"
                            )
                    }
                }
        }

        lifecycleScope.launch {
            addItemViewModel.addItemEvent
                .collect { fabEvent ->
                    when (fabEvent) {
                        is AddItemEvent.Error ->
                            Log.e(TAG(), "${fabEvent.message}: ${fabEvent.throwable.message}")

                        is AddItemEvent.Info ->
                            Log.e(TAG(), fabEvent.message)
                    }
                }
        }

        lifecycleScope.launch {
            subscriptionTypeViewModel.subscriptionTypeEvent
                .collect { subscriptionTypeEvent ->
                    when (subscriptionTypeEvent) {
                        is SubscriptionTypeEvent.Error ->
                            Log.e(
                                TAG(),
                                "${subscriptionTypeEvent.message}: ${subscriptionTypeEvent.throwable.message}"
                            )

                        is SubscriptionTypeEvent.Info ->
                            Log.i(TAG(), subscriptionTypeEvent.message)

                        SubscriptionTypeEvent.PermissionsEvent -> {
                            Log.i(TAG(), "Tried to switch subscription while offline.")
                            Toast.makeText(
                                this@ComposeItemActivity,
                                getString(R.string.unable_to_switch),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        setContent {
            MyApplicationTheme {
                TaskListScaffold(
                    repository,
                    toolbarViewModel,
                    addItemViewModel,
                    subscriptionTypeViewModel,
                    taskViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.close()
    }
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun TaskListScaffold(
    repository: SyncRepository,
    toolbarViewModel: ToolbarViewModel,
    addItemViewModel: AddItemViewModel,
    subscriptionTypeViewModel: SubscriptionTypeViewModel,
    taskViewModel: TaskViewModel
) {
    val annotatedLinkString = buildAnnotatedString {
        val linkString = "To see your changes in Atlas, tap here."
        val startIndex = linkString.indexOf("here")
        val endIndex = startIndex + 4
        append(linkString)
        addStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            ), start = startIndex, end = endIndex
        )
        addStringAnnotation(
            tag = "URL",
            annotation = stringResource(id = R.string.realm_data_explorer_link),
            start = startIndex,
            end = endIndex
        )
    }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = { TaskAppToolbar(toolbarViewModel) },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.LightGray
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(0.dp, 0.dp, 0.dp, 0.dp),
                ) {
                    ClickableText(
                        modifier = Modifier
                            .background(Color.LightGray)
                            .padding(0.dp, 44.dp, 0.dp, 0.dp)
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        text = annotatedLinkString,
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        onClick = {
                            annotatedLinkString
                                .getStringAnnotations("URL", it, it)
                                .firstOrNull()?.let { stringAnnotation ->
                                    uriHandler.openUri(stringAnnotation.item)
                                }
                        }
                    )
                    Text(
                        text = stringResource(R.string.sync_message),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = Color.Black
                    )
                }
            }
            Divider(color = Color.Black, modifier = Modifier.fillMaxWidth())
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                contentColor = Color.White,
                containerColor = Purple200,
                onClick = {
                    addItemViewModel.openAddTaskDialog()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }

            if (addItemViewModel.addItemPopupVisible.value) {
                AddItemPrompt(addItemViewModel)
            }
        },
        content = {
            Column {
                Spacer(modifier = Modifier.height(61.dp))
                Divider(color = Color.Red, modifier = Modifier.fillMaxWidth())
                ShowMyOwnTasks(subscriptionTypeViewModel, toolbarViewModel)
                TaskList(repository, taskViewModel)

            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ItemActivityPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val tasks = (1..30).map { index ->
            MockRepository.getMockTask(index)
        }.toMutableStateList()

        MyApplicationTheme {
            TaskListScaffold(
                repository,
                ToolbarViewModel(repository),
                AddItemViewModel(repository),
                SubscriptionTypeViewModel(repository),
                TaskViewModel(repository, tasks)
            )
        }
    }
}
