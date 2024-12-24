package com.example.quicktalk.Screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.quicktalk.QTViewModel


@Composable
fun ProfileScreen(navController: NavHostController, viewModel: QTViewModel) {
    BottomNavigationMenu(selectedItem = BottomNavigationItem.PROFILE,navController=navController)
}