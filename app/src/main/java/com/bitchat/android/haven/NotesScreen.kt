package com.bitchat.android.haven

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Decoy notes pre-seeded to look like a real notes app in use
private val DECOY_NOTES = listOf(
    "Call mama Sunday evening",
    "Electricity bill - pay before 15th",
    "Milk, bread, eggs, onions",
    "Dr. appointment - Tuesday 11am",
    "Password wifi: Sunshine@2024",
    "Meeting notes - Q3 targets discussed",
    "Book: Atomic Habits - chapter 6",
    "Birthday - Priya - Oct 12",
    "Return Amazon package by Friday",
    "Gym - Mon Wed Fri 7am"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var showWrongPin by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(DECOY_NOTES.toMutableList()) }

    fun handleSave() {
        if (inputText.isBlank()) return
        when (HavenPreferences.checkPin(context, inputText.trim())) {
            HavenPreferences.PinResult.REAL -> onUnlocked()
            HavenPreferences.PinResult.DURESS -> {
                // Duress: wipe real data silently, stay in notes
                DuressManager.wipeRealData(context)
                inputText = ""
            }
            HavenPreferences.PinResult.WRONG -> {
                notes = (notes + inputText.trim()).toMutableList()
                inputText = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { handleSave() },
                containerColor = Color(0xFF007AFF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF2F2F7)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    showWrongPin = false
                },
                placeholder = { Text("New note…", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF007AFF),
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleSave() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes.reversed()) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = note,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 15.sp,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                }
            }
        }
    }
}
