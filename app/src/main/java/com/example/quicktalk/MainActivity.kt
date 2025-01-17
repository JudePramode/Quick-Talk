package com.example.quicktalk

import ChatListScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.quicktalk.Screens.LoginScreen
import com.example.quicktalk.Screens.ProfileScreen
import com.example.quicktalk.Screens.SignUpScreen
import com.example.quicktalk.Screens.SingleChatScreen
import com.example.quicktalk.Screens.SingleStatusScreen
import com.example.quicktalk.Screens.StatusScreen
import com.example.quicktalk.ui.theme.QuickTalkTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(chatId: String) = "singleChat/$chatId"
    }
    object StatusList : DestinationScreen("statusList")
    object SingleStatus : DestinationScreen("singleStatus/{userId}") {
        fun createRoute(userId: String) = "singleStatus/$userId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            QuickTalkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {
        val navController = rememberNavController()
        val viewModel = hiltViewModel<QTViewModel>() // Hilt injects ViewModel

        NavHost(
            navController = navController,
            startDestination = DestinationScreen.SignUp.route
        ) {
            composable(DestinationScreen.SignUp.route) {
                SignUpScreen(navController, viewModel)
            }
            composable(DestinationScreen.Login.route) {
                LoginScreen(navController = navController, vm = viewModel)
            }
            composable(DestinationScreen.ChatList.route) {
                ChatListScreen(navController, viewModel)
            }
            composable(DestinationScreen.SingleChat.route) {
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    SingleChatScreen(
                        navController = navController,
                        viewModel = viewModel,
                        chatId = chatId
                    )
                }
            }


            composable(DestinationScreen.StatusList.route) {
                StatusScreen(navController, viewModel)
            }

            composable(DestinationScreen.Profile.route) {
                ProfileScreen(navController, viewModel)
            }

            composable (DestinationScreen.SingleStatus.route){
                val userId = it.arguments?.getString("userId")
                userId?.let {

                    SingleStatusScreen(navController=navController, viewModel=viewModel, userId = it)

                }
            }


        }
    }

}

