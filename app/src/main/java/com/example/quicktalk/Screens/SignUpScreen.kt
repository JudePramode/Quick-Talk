package com.example.quicktalk.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quicktalk.CheckSignedIn
import com.example.quicktalk.CommonProgressBar
import com.example.quicktalk.DestinationScreen
import com.example.quicktalk.QTViewModel
import com.example.quicktalk.R
import com.example.quicktalk.navigateTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, vm: QTViewModel)

{


    CheckSignedIn(vm=vm, navController=navController)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            , horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nameState = remember {
                mutableStateOf(TextFieldValue())
            }

            val numberState = remember {
                mutableStateOf(TextFieldValue())
            }

            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }

            val passwordState = remember {
                mutableStateOf(TextFieldValue())
            }

            val focus= LocalFocusManager.current

            Image(
                painter = painterResource(id = R.drawable.quicktalk),
                contentDescription = null,
                modifier = Modifier
                    .width(180.dp)
                    .padding(top = 40.dp)
                    .padding(8.dp)
            )
            Text(
                text = "Sign Up",
                modifier = Modifier.padding(top = 32.dp).padding(8.dp),
                fontSize = 25.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF600101),


            )

           OutlinedTextField(
                value = nameState.value,
                onValueChange = {
                nameState.value= it
            },
                label = {Text(text="Name")},
                modifier = Modifier.padding(top=32.dp).padding(8.dp),
               shape = RoundedCornerShape(12.dp),
            )

            OutlinedTextField(
                value = numberState.value,
                onValueChange = {
                    numberState.value= it
                },
                label = {Text(text="Phone Number")},
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(12.dp),
            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState.value= it
                },
                label = {Text(text="Email")},
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(12.dp),
            )

            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value= it
                },
                label = {Text(text="Password")},
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(12.dp),
            )

            Button(onClick = {vm.signUp(
                name = nameState.value.text,
               number =  numberState.value.text,
                email = emailState.value.text,
                password = passwordState.value.text,
            )

            },
                modifier = Modifier.padding(top = 40.dp).padding(8.dp).widthIn(250.dp,300.dp).heightIn(50.dp,60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF730101),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.LightGray,


                )
            ) {
                Text(text = "Sign Up")
            }

            Text(text = "Already have an Account? Go to login",
                color = Color(0xFF757575),
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
                    .clickable {
                        navigateTo(navController,DestinationScreen.Login.route)
                    }

                )






        }



    }

    if(vm.inProgress.value){
        CommonProgressBar()
    }

}
