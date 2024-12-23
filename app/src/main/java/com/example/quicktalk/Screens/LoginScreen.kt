package com.example.quicktalk.Screens

import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.quicktalk.QTViewModel

@Composable
fun LoginScreen(navController: NavHostController, viewModel: QTViewModel) {
    Text(
        text = "Hi this is Login Screen",
        modifier = Modifier.background(Color.Blue)
    )
}
