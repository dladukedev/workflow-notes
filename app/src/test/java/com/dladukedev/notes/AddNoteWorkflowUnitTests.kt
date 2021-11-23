package com.dladukedev.notes

import com.dladukedev.notes.workflow.AddNoteWorkflow
import com.squareup.workflow1.applyTo
import junit.framework.Assert.*
import org.junit.Test

class AddNoteWorkflowUnitTests {
    @Test
    fun `when user submits note, the state is updated to submitting`() {
        val workflow = AddNoteWorkflow()
        val newNote = "aadfsadfsad"

        val startState = AddNoteWorkflow.State.Idle(newNote)
        val action = workflow.onSubmit()
        val (state, output) = action.applyTo(state = startState, props = AddNoteWorkflow.Props)

        // No output is expected when the name changes.
        assertNull(output)

        // The name has been updated from the action.
        assertTrue(state is AddNoteWorkflow.State.Submitting)
        assertEquals(newNote, state.newNoteContent)
    }

    @Test
    fun `when user presses the back button, the output is back`() {
        val workflow = AddNoteWorkflow()

        val startState = AddNoteWorkflow.State.Idle("")
        val action = workflow.onBackPressed()
        val (_, output) = action.applyTo(state = startState, props = AddNoteWorkflow.Props)

        assertTrue(output?.value is AddNoteWorkflow.Output.Back)

    }
}