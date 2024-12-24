package com.example.quicktalk.Screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.quicktalk.QTViewModel

@Composable
fun ChatListScreen(navController: NavHostController, viewModel: QTViewModel) {
    Text(text="chat List Screen")
    BottomNavigationMenu(selectedItem = BottomNavigationItem.CHATLIST, navController=navController)
}