package com.example.quicktalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quicktalk.Screens.LoginScreen
import com.example.quicktalk.Screens.SignUpScreen
import com.example.quicktalk.ui.theme.QuickTalkTheme

sealed class DestinationScreen(var route: String){
    object SignUp: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Profile: DestinationScreen("profile")
    object ChatList: DestinationScreen("chatList")
    object SingleChat: DestinationScreen("singleChat/{chatId}"){
        fun CreateRoute(id:String) = "singleChat/$id"

    }
    object StatusList: DestinationScreen("statusList")
    object SingleStatus: DestinationScreen("singleStatus/{userId}"){
        fun CreateRoute(useId:String) = "singleStatus/$useId"

    }


}





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickTalkTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                    ){
                    ChatAppNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation(){

        val navController = rememberNavController()
        var vm=hiltViewModel<QTViewModel>()
        NavHost(navController= navController, startDestination = DestinationScreen.SignUp.route){
            composable(DestinationScreen.SignUp.route){
                SignUpScreen(navController,vm)
            }

            composable(DestinationScreen.Login.route){
                LoginScreen()
            }





        }



    }

}
