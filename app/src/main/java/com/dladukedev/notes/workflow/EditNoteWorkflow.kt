package com.dladukedev.notes.workflow

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dladukedev.notes.data.NotesRepository
import com.dladukedev.notes.data.NotesRepositoryImpl
import com.dladukedev.notes.ui.components.TextInput
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.composeViewFactory
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class EditNoteWorkflow(
    private val notesRepository: NotesRepository = NotesRepositoryImpl,
) : StatefulWorkflow<EditNoteWorkflow.Props, EditNoteWorkflow.State, EditNoteWorkflow.Output, EditNoteWorkflow.Screen>() {
    data class Props(val noteId: Long, val noteContent: String)

    sealed class State(
        open val updatedNoteContent: String
    ) : Parcelable {
        @Parcelize
        data class Idle(override val updatedNoteContent: String) : State(updatedNoteContent)

        @Parcelize
        data class Submitting(override val updatedNoteContent: String) : State(updatedNoteContent)
    }

    sealed class Output {
        object Back : Output()
    }

    data class Screen(
        val updatedNoteContent: String,
        val onContentChanged: (String) -> Unit,
        val onSubmit: () -> Unit,
        val onBackPressed: () -> Unit,
        val isSubmitting: Boolean,
    )

    override fun initialState(props: Props, snapshot: Snapshot?): State {
        return snapshot?.toParcelable() ?: State.Idle(props.noteContent)
    }

    override fun render(renderProps: Props, renderState: State, context: RenderContext): Screen {
        return when (renderState) {
            is State.Idle -> {
                Screen(
                    updatedNoteContent = renderState.updatedNoteContent,
                    onContentChanged = { note -> context.actionSink.send(onNoteContentChanged(note)) },
                    onSubmit = { context.actionSink.send(onSubmit()) },
                    onBackPressed = { context.actionSink.send(onBackPressed()) },
                    isSubmitting = false
                )
            }
            is State.Submitting -> {
                context.runningWorker(Worker.from {
                    notesRepository.editNote(
                        renderProps.noteId,
                        renderState.updatedNoteContent
                    )
                }, "key") {
                    onNoteUpdated()
                }

                Screen(
                    updatedNoteContent = renderState.updatedNoteContent,
                    onContentChanged = {},
                    onSubmit = {},
                    onBackPressed = {},
                    isSubmitting = true
                )
            }
        }
    }

    private fun onNoteContentChanged(newContent: String) = action {
        state = State.Idle(newContent)
    }

    private fun onNoteUpdated() = action {
        setOutput(Output.Back)
    }

    private fun onBackPressed() = action {
        setOutput(Output.Back)
    }

    private fun onSubmit() = action {
        state = State.Submitting(state.updatedNoteContent)
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    companion object {
        @WorkflowUiExperimentalApi
        val View = composeViewFactory<Screen> { rendering, _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                BackHandler(onBack = rendering.onBackPressed)

                LazyColumn {
                    item {
                        TextInput(
                            label = "Note",
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            value = rendering.updatedNoteContent,
                            onValueChange = rendering.onContentChanged,
                            enabled = !rendering.isSubmitting,
                        )
                    }
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            FilledTonalButton(
                                onClick = { rendering.onSubmit() },
                                enabled = !rendering.isSubmitting && rendering.updatedNoteContent.isNotEmpty(),
                                modifier = Modifier.align(Alignment.BottomEnd),
                            ) {
                                if (rendering.isSubmitting) {
                                    Text(text = "Saving...")
                                } else {
                                    Text(text = "Update")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}