package com.dladukedev.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dladukedev.notes.ui.theme.NotesTheme
import com.dladukedev.notes.workflow.*
import com.squareup.workflow1.ui.NamedViewFactory
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backstack.BackStackContainer
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.compose.renderAsState

@WorkflowUiExperimentalApi
private val viewRegistry = ViewRegistry(
    BackStackContainer,
    NamedViewFactory,
    NotesListWorkflow.View,
    NoteDetailWorkflow.View,
    AddNoteWorkflow.View,
)

@WorkflowUiExperimentalApi
private val viewEnvironment = ViewEnvironment(mapOf(ViewRegistry to viewRegistry))

@WorkflowUiExperimentalApi
class MainActivity : ComponentActivity() {
    private val notesWorkflow = NotesWorkflow()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val rendering by notesWorkflow.renderAsState(props = Unit, onOutput = {})
                    WorkflowRendering(rendering, viewEnvironment)
                }
            }
        }
    }
}
