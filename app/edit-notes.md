# Implementation Notes
- Make stateful workflow
- Stub all methods and types
- Add Props & Output
- Update Notes Workflow to handle back and start
- Setup State, Output, and Screen
- Add "on" Methods, onNoteContentChanged, onNoteUpdated, onBackPressed, onSubmit
- Implement render function
- inject repository and add runningWorker
- uncomment view and add to MainActivity
- implement snapshotState and update initialState

# Main Activity
    EditNoteWorkflow.View

# Notes Workflow
    is State.ViewingEditNote -> {
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
    
        val editNoteScreen = context.renderChild(
            EditNoteWorkflow(),
            EditNoteWorkflow.Props(renderState.noteId, renderState.content),
            "EditNoteWorkflow${renderState.noteId}"
        ) { output ->
            when (output) {
                EditNoteWorkflow.Output.Back -> {
                    onViewNoteDetails(renderState.noteId)
                }
            }
        }
    
        backstackScreens = listOf(notesListScreen, noteDetailsScreen, editNoteScreen)
    }


# Edit Note Workflow

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