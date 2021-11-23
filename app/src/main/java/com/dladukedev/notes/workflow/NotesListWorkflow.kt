package com.dladukedev.notes.workflow

import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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

class NotesListWorkflow(
    private val notesRepository: NotesRepository = NotesRepositoryImpl
) : StatefulWorkflow<NotesListWorkflow.Props, NotesListWorkflow.State, NotesListWorkflow.Output, NotesListWorkflow.Screen>() {
    object Props

    sealed class State: Parcelable {
        @Parcelize
        object Initializing : State()
        @Parcelize
        data class Idle(val notes: List<Note>) : State()
    }

    sealed class Output {
        object ViewAddNote : Output()
        data class ViewNoteDetails(val noteId: Long) : Output()
    }

    data class Screen(
        val isLoading: Boolean,
        val notes: List<Note>?,
        val onAddNoteSelected: () -> Unit,
        val onNoteSelected: (Long) -> Unit,
    )

    override fun initialState(props: Props, snapshot: Snapshot?): State {
        return snapshot?.toParcelable() ?: State.Initializing
    }

    override fun render(renderProps: Props, renderState: State, context: RenderContext): Screen {
        context.runningWorker(notesRepository.subscribeToNotes().asWorker(), "") { notes ->
            onNotesLoaded(notes)
        }

        return when (renderState) {
            State.Initializing -> {
                Screen(
                    isLoading = true,
                    notes = null,
                    onAddNoteSelected = { context.actionSink.send(onBeginAddNote()) },
                    onNoteSelected = { noteId -> context.actionSink.send(onNoteSelected(noteId)) },
                )
            }
            is State.Idle -> {
                Screen(
                    isLoading = false,
                    notes = renderState.notes,
                    onAddNoteSelected = { context.actionSink.send(onBeginAddNote()) },
                    onNoteSelected = { noteId -> context.actionSink.send(onNoteSelected(noteId)) }
                )
            }
        }
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    private fun onNotesLoaded(notes: List<Note>) = action {
        state = State.Idle(notes)
    }

    private fun onBeginAddNote() = action {
        setOutput(Output.ViewAddNote)
    }

    private fun onNoteSelected(noteId: Long) = action {
        setOutput(Output.ViewNoteDetails(noteId))
    }

    companion object {
        @WorkflowUiExperimentalApi
        val View = composeViewFactory<Screen> { rendering, _ ->
            when {
                rendering.isLoading -> {
                    LoadingView()
                }
                else -> {
                    Box(modifier = Modifier
                        .fillMaxSize()) {
                        if(rendering.notes.isNullOrEmpty()) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(text = "Add a New Note \r\n\r\n↓↓↓", style = MaterialTheme.typography.displayMedium, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                                items(rendering.notes) { note ->
                                    Surface(
                                        tonalElevation = 2.dp,
                                        modifier = Modifier
                                            .clickable { rendering.onNoteSelected(note.id) }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(20.0.dp)
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(16.dp, 8.dp),
                                            text = note.content
                                        )
                                    }
                                }
                                item {
                                    Box(modifier = Modifier.height(128.dp))
                                }
                            }
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                            onClick = { rendering.onAddNoteSelected() }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}