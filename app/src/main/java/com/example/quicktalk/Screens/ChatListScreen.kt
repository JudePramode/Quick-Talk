package com.example.quicktalk.Screens


import android.icu.text.CaseMap
import android.icu.text.CaseMap.Title
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScopeInstance.weight

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quicktalk.CommonImage
import com.example.quicktalk.CommonProgressBar
import com.example.quicktalk.CommonRow
import com.example.quicktalk.DestinationScreen
import com.example.quicktalk.QTViewModel
import com.example.quicktalk.TitleText
import com.example.quicktalk.navigateTo
import com.google.ai.client.generativeai.type.content

@Composable
fun ChatListScreen(navController: NavHostController, viewModel: QTViewModel) {
    val inProgress = viewModel.inProgress // Assuming `inProgress` is properly defined in QTViewModel
    val showDialog = remember { mutableStateOf(false) } // Single showDialog state

    Scaffold(

        floatingActionButton = {
            FAB(

                showDialog = showDialog.value,
                onFabClick = { showDialog.value = true },
                onDismiss = { showDialog.value = false },
                onAddChat = { chatNumber ->
                    viewModel.onAddChat(chatNumber) // Assuming `onAddChat` is defined in QTViewModel
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
        )

        {
            if (inProgress.value) {
                CommonProgressBar() // Show a progress bar when loading
            } else {
                LazyColumn ( modifier = Modifier.weight(1f)){
                    items (chats){
                            chat->
                        val chatUser=if (chat.user1.userId==userData.userId){
                            chat.user2
                        }else{
                            chat.user1
                        }
                        CommonRow(imageUrl = chatUser.imageUrl, name =chatUser.name) {
                            chat.chatId?.let{
                                navigateTo(navController,DestinationScreen.SingleChat.createRoute(id=it))


                            }
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

    // Display the dialog if showDialog is true
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
        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Chat", tint = Color.White)
    }
}
