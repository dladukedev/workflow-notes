package com.dladukedev.notes.workflow

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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

class AddNoteWorkflow(
    private val notesRepository: NotesRepository = NotesRepositoryImpl,
) : StatefulWorkflow<AddNoteWorkflow.Props, AddNoteWorkflow.State, AddNoteWorkflow.Output, AddNoteWorkflow.Screen>() {
    object Props
    sealed class State(open val newNoteContent: String) : Parcelable {
        @Parcelize
        data class Idle(override val newNoteContent: String) : State(newNoteContent)

        @Parcelize
        data class Submitting(override val newNoteContent: String) : State(newNoteContent)
    }

    sealed class Output {
        object Back : Output()
    }

    data class Screen(
        val newNoteContent: String,
        val onContentChanged: (String) -> Unit,
        val onSubmit: () -> Unit,
        val onBackPressed: () -> Unit,
        val isSubmitting: Boolean,
    )

    override fun initialState(props: Props, snapshot: Snapshot?): State {
        return snapshot?.toParcelable() ?: State.Idle("")
    }

    override fun render(renderProps: Props, renderState: State, context: RenderContext): Screen {
        return when (renderState) {
            is State.Idle -> {
                Screen(
                    newNoteContent = renderState.newNoteContent,
                    onContentChanged = { newContent ->
                        context.actionSink.send(
                            onNoteContentChanged(
                                newContent
                            )
                        )
                    },
                    onSubmit = { context.actionSink.send(onSubmit()) },
                    onBackPressed = { context.actionSink.send(onBackPressed()) },
                    isSubmitting = false,
                )
            }
            is State.Submitting -> {
                context.runningWorker(
                    Worker.from { notesRepository.addNote(renderState.newNoteContent) },
                    "key"
                ) {
                    onNoteAdded()
                }

                Screen(
                    newNoteContent = renderState.newNoteContent,
                    onContentChanged = { /* no-op */ },
                    onSubmit = { /* no-op */ },
                    onBackPressed = { /* no-op */ },
                    isSubmitting = true,
                )
            }
        }
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    private fun onNoteContentChanged(newContent: String) = action {
        state = State.Idle(newContent)
    }

    private fun onNoteAdded() = action {
        setOutput(Output.Back)
    }

    fun onBackPressed() = action {
        setOutput(Output.Back)
    }

    fun onSubmit() = action {
        state = State.Submitting(state.newNoteContent)
    }

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
                            label = "New Note",
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            value = rendering.newNoteContent,
                            onValueChange = rendering.onContentChanged,
                            enabled = !rendering.isSubmitting,
                        )
                    }
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            FilledTonalButton(
                                onClick = { rendering.onSubmit() },
                                enabled = !rendering.isSubmitting && rendering.newNoteContent.isNotEmpty(),
                                modifier = Modifier.align(Alignment.BottomEnd)
                            ) {
                                if (rendering.isSubmitting) {
                                    Text(text = "Saving...")
                                } else {
                                    Text(text = "Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}