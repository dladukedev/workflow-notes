package com.dladukedev.notes.data

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime

interface NotesRepository {
    fun subscribeToNotes(): Flow<List<Note>>
    fun subscribeToNote(noteId: Long): Flow<Note>
    suspend fun browseNotes(): List<Note>
    suspend fun readNote(noteId: Long): Note
    suspend fun editNote(noteId: Long, note: String): Note
    suspend fun addNote(note: String): Note
    suspend fun deleteNote(noteId: Long): Boolean
}

object NotesRepositoryImpl : NotesRepository {
    private const val delayMs = 1000L

    private var notes = listOf<Note>()
        set(value) {
            field = value
            notesFlow.update {
                field
            }
        }

    private val notesFlow = MutableStateFlow(notes)

    private fun generateId(): Long {
        val largestId = notes.maxOfOrNull { it.id } ?: 0

        return largestId + 1
    }

    override fun subscribeToNotes(): Flow<List<Note>> {
        return notesFlow
    }

    override fun subscribeToNote(noteId: Long): Flow<Note> {
        return notesFlow.map { notes -> notes.single { note -> note.id == noteId } }
    }

    override suspend fun browseNotes(): List<Note> {
        delay(delayMs)
        return notes
    }

    override suspend fun readNote(noteId: Long): Note {
        delay(delayMs)
        return notes.single { it.id == noteId }
    }

    override suspend fun addNote(note: String): Note {
        delay(delayMs)
        val newNote = Note(
            generateId(),
            note,
            LocalDateTime.now(),
            LocalDateTime.now(),
        )

        notes = notes + listOf(newNote)

        return newNote
    }

    override suspend fun editNote(noteId: Long, note: String): Note {
        delay(delayMs)
        val existingNote = notes.single { it.id == noteId }
        val updatedNote = existingNote.copy(content = note, edited = LocalDateTime.now())

        notes = notes.map { mapNote ->
            if(mapNote.id == noteId) {
                updatedNote
            } else {
                mapNote
            }
        }

        return updatedNote
    }

    override suspend fun deleteNote(noteId: Long): Boolean {
        delay(delayMs)
        return try {
            notes = notes.filter { note -> note.id != noteId }

            true
        } catch (e: Exception) {
            Log.e("NotesRepo", e.message ?: "No Error Message Provided")
            false
        }
    }

    init {
        val now = LocalDateTime.now()
        notes = mutableListOf(
            Note(1, "Temporary Note", now, now),
            Note(2, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", now, now),
            Note(3, "Errands", now, now),
        )
    }
}

