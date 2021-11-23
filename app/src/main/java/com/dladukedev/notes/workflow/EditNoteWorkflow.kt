package com.dladukedev.notes.workflow

class EditNoteWorkflow {

//    companion object {
//        @WorkflowUiExperimentalApi
//        val View = composeViewFactory<Screen> { rendering, _ ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//            ) {
//                BackHandler(onBack = rendering.onBackPressed)
//
//                LazyColumn {
//                    item {
//                        TextInput(
//                            label = "Note",
//                            modifier = Modifier
//                                .padding(bottom = 8.dp)
//                                .fillMaxWidth(),
//                            value = rendering.updatedNoteContent,
//                            onValueChange = rendering.onContentChanged,
//                            enabled = !rendering.isSubmitting,
//                        )
//                    }
//                    item {
//                        Box(modifier = Modifier.fillMaxWidth()) {
//                            FilledTonalButton(
//                                onClick = { rendering.onSubmit() },
//                                enabled = !rendering.isSubmitting && rendering.updatedNoteContent.isNotEmpty(),
//                                modifier = Modifier.align(Alignment.BottomEnd),
//                            ) {
//                                if (rendering.isSubmitting) {
//                                    Text(text = "Saving...")
//                                } else {
//                                    Text(text = "Update")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}