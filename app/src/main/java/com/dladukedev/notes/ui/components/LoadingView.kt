package com.dladukedev.notes.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text("Loading...", modifier = Modifier.align(Alignment.Center))
    }
}