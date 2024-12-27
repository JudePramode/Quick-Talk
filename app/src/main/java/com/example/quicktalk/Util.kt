package com.example.quicktalk

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import coil3.compose.rememberAsyncImagePainter

fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) // Clears back stack to the root
        launchSingleTop = true
    }
}

@Composable
fun CommonProgressBar() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun CommonDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier.size(100.dp),
    placeholder: Painter? = painterResource(id = R.drawable.profile),
    error: Painter? = painterResource(id = R.drawable.profile)
) {
    Image(
        painter = rememberAsyncImagePainter(
            model = data,
            placeholder = placeholder,
            error = error,
            contentScale = ContentScale.Crop // Ensures the image fills the circular shape
        ),
        contentDescription = null,
        modifier = modifier
    )
}




@Composable
fun CheckSignedIn(vm: QTViewModel, navController: NavController) {
    val alreadySignedIn = remember { mutableStateOf(false) }
    val signIn = vm.signIn.value

    if (signIn && !alreadySignedIn.value) {
        alreadySignedIn.value = true
        navController.navigate(DestinationScreen.ChatList.route) {
            popUpTo(navController.graph.startDestinationId) // Clears back stack
        }
    }
}

@Composable
fun TitleText (txt:String){
    Text(txt, fontWeight = FontWeight.Bold, fontSize = 35.sp, modifier = Modifier.padding(10.dp).padding(top = 10.dp) )
}

@Composable
fun CommonRow(imageUrl:String?,name:String?,onItemClick:()->Unit){
    Row (modifier = Modifier.fillMaxSize().height(80.dp).clickable { onItemClick.invoke() }, verticalAlignment = Alignment.CenterVertically){
        CommonImage(data = imageUrl, modifier = Modifier.padding(8.dp).size(50.dp).clip(CircleShape).background(
            Color.LightGray))
        Text(text = name?:"---",
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
    }

}