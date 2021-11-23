package com.dladukedev.notes.workflow

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dladukedev.notes.data.Note
import com.dladukedev.notes.data.NotesRepository
import com.dladukedev.notes.data.NotesRepositoryImpl
import com.dladukedev.notes.ui.components.LoadingView
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.composeViewFactory
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class NoteDetailWorkflow(
    private val notesRepository: NotesRepository = NotesRepositoryImpl,
) : StatefulWorkflow<NoteDetailWorkflow.Props, NoteDetailWorkflow.State, NoteDetailWorkflow.Output, NoteDetailWorkflow.Screen>() {
    data class Props(val noteId: Long)
    sealed class State : Parcelable {
        @Parcelize
        object Initializing : State()

        @Parcelize
        data class Idle(val note: Note) : State()
    }

    sealed class Output {
        object Back : Output()
        data class EditNote(val noteId: Long, val noteContent: String) : Output()
    }

    data class Screen(
        val isLoading: Boolean,
        val note: Note?,
        val onEditClick: (Note) -> Unit,
        val onBackPressed: () -> Unit,
    )

    override fun initialState(props: Props, snapshot: Snapshot?): State {
        return snapshot?.toParcelable() ?: State.Initializing
    }

    override fun render(renderProps: Props, renderState: State, context: RenderContext): Screen {
        context.runningWorker(
            notesRepository.subscribeToNote(renderProps.noteId).asWorker(),
            "${renderProps.noteId}"
        ) { note ->
            onNoteLoaded(note)
        }

        return when (renderState) {
            State.Initializing -> {
                Screen(
                    isLoading = true,
                    note = null,
                    onEditClick = { /* no-op */ },
                    onBackPressed = { context.actionSink.send(onBack()) },
                )
            }
            is State.Idle -> {
                Screen(
                    isLoading = false,
                    note = renderState.note,
                    onEditClick = { note -> context.actionSink.send(onEditNote(note)) },
                    onBackPressed = { context.actionSink.send(onBack()) },
                )
            }
        }
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    private fun onNoteLoaded(note: Note) = action {
        state = State.Idle(note)
    }

    private fun onEditNote(note: Note) = action {
        setOutput(Output.EditNote(note.id, note.content))
    }

    private fun onBack() = action {
        setOutput(Output.Back)
    }

    companion object {
        @WorkflowUiExperimentalApi
        val View = composeViewFactory<Screen> { rendering, _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackHandler(onBack = rendering.onBackPressed)

                when {
                    rendering.isLoading -> {
                        LoadingView()
                    }
                    rendering.note != null -> {
                        LazyColumn {
                            item {
                                Text(
                                    text = rendering.note.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = { rendering.onEditClick(rendering.note) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(32.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}