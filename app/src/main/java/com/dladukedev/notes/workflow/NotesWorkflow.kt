package com.dladukedev.notes.workflow

import android.os.Parcelable
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backstack.BackStackScreen
import com.squareup.workflow1.ui.backstack.toBackStackScreen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

@OptIn(WorkflowUiExperimentalApi::class)
class NotesWorkflow : StatefulWorkflow<Unit, NotesWorkflow.State, Nothing, BackStackScreen<Any>>() {
    sealed class State : Parcelable {
        @Parcelize
        object ViewingNotesList : State()

        @Parcelize
        object ViewingAddNote : State()

        @Parcelize
        data class ViewingNoteDetail(val noteId: Long) : State()

        @Parcelize
        data class ViewingEditNote(val noteId: Long, val content: String) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State {
        return if (snapshot != null) {
            snapshot.toParcelable() ?: State.ViewingNotesList
        } else {
            State.ViewingNotesList
        }
    }

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): BackStackScreen<Any> {
        var backstackScreens = listOf<Any>()

        val notesListScreen = context.renderChild(
            NotesListWorkflow(),
            NotesListWorkflow.Props,
            "NotesListWorkflow"
        ) { output ->
            when (output) {
                NotesListWorkflow.Output.ViewAddNote -> {
                    onViewAddNote()
                }
                is NotesListWorkflow.Output.ViewNoteDetails -> {
                    onViewNoteDetails(output.noteId)
                }
            }
        }

        when (renderState) {
            State.ViewingNotesList -> {
                backstackScreens = listOf(notesListScreen)
            }
            State.ViewingAddNote -> {
                val addNotesScreen = context.renderChild(
                    AddNoteWorkflow(),
                    AddNoteWorkflow.Props,
                    "AddNoteWorkflow"
                ) { output ->
                    when (output) {
                        AddNoteWorkflow.Output.Back -> {
                            onViewNotesList()
                        }
                    }
                }

                backstackScreens = listOf(notesListScreen, addNotesScreen)
            }
            is State.ViewingNoteDetail -> {
                val noteDetailsScreen = context.renderChild(
                    NoteDetailWorkflow(),
                    NoteDetailWorkflow.Props(renderState.noteId),
                    "NoteDetailWorkflow${renderState.noteId}"
                ) { output ->
                    when (output) {
                        NoteDetailWorkflow.Output.Back -> {
                            onViewNotesList()
                        }
                        is NoteDetailWorkflow.Output.EditNote -> {
                            onViewEditNote(output.noteId, output.noteContent)
                        }
                    }
                }

                backstackScreens = listOf(notesListScreen, noteDetailsScreen)

            }

        }

        return backstackScreens.toBackStackScreen()
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()


    private fun onViewNotesList() = action {
        state = State.ViewingNotesList
    }

    private fun onViewAddNote() = action {
        state = State.ViewingAddNote
    }

    private fun onViewNoteDetails(noteId: Long) = action {
        state = State.ViewingNoteDetail(noteId)
    }

    private fun onViewEditNote(noteId: Long, content: String) = action {
        state = State.ViewingEditNote(noteId, content)
    }
}