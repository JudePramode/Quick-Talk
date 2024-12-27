package com.example.quicktalk.Screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quicktalk.*

@Composable
fun ChatListScreen(navController: NavHostController, viewModel: QTViewModel) {
    val inProgress = viewModel.inProgress // Loading state
    val chats = viewModel.chats // List of chats (State<List<ChatData>>)
    val userData = viewModel.userData // Current user (State<UserData>)
    val showDialog = remember { mutableStateOf(false) } // Show dialog state

    Scaffold(
        floatingActionButton = {
            FAB(
                showDialog = showDialog.value,
                onFabClick = { showDialog.value = true },
                onDismiss = { showDialog.value = false },
                onAddChat = { chatNumber ->
                    viewModel.onAddChat(chatNumber) // Add a new chat
                    showDialog.value = false
                }
            )
        },
        bottomBar = {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.CHATLIST,
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (inProgress.value) {
                CommonProgressBar() // Display progress bar while loading
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(start = 25.dp).padding(end = 20.dp)
                ) {
                    items(chats.value) { chat -> // Iterate through chat list
                        val chatUser = if (chat.user1.userId == userData.value?.userId) {
                            chat.user2
                        } else {
                            chat.user1
                        }

                        CommonRow(
                            imageUrl = chatUser.imageUrl,
                            name = chatUser.name
                        ) {
                            val chatId = chat.chatId ?: return@CommonRow // Skip if chatId is null
                            navigateTo(navController, DestinationScreen.SingleChat.createRoute(chatId))

                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                addChatNumber.value = ""
            },
            confirmButton = {
                Button(onClick = {
                    onAddChat(addChatNumber.value)
                    onDismiss()
                    addChatNumber.value = ""
                }) {
                    Text(text = "Add Chat")
                }
            },
            title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value,
                    onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        )
    }

    FloatingActionButton(
        onClick = { onFabClick() },
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add Chat",
            tint = Color.White
        )
    }
}
