package com.dladukedev.notes.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Note(
    val id: Long,
    val content: String,
    val created: LocalDateTime,
    val edited: LocalDateTime,
): Parcelable