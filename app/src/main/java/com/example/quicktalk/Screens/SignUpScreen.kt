package com.example.quicktalk.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.quicktalk.DestinationScreen
import com.example.quicktalk.QTViewModel

@Composable
fun SignUpScreen(navController: NavController, vm: QTViewModel) {

    Box(modifier = Modifier.fillMaxSize()){

        Column (modifier = Modifier.fillMaxSize().wrapContentHeight().verticalScroll(
            rememberScrollState()
        )){

            Image(painter = , contentDiscription=null)



        }

    }


}