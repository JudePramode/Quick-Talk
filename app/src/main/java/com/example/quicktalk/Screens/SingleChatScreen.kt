package com.example.quicktalk.Screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

@Composable
fun SingleChatScreen(navController: NavController,viewModel: ViewModel, chatId:String) {
    Text(text = chatId)
}