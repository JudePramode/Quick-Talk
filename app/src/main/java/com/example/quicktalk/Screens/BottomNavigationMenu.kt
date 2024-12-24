package com.example.quicktalk.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quicktalk.DestinationScreen
import com.example.quicktalk.R
import com.example.quicktalk.navigateTo

enum class BottomNavigationItem(val icon: Int, val navDestination: DestinationScreen) {
    CHATLIST(R.drawable.chat, DestinationScreen.ChatList),
    STATUSLIST(R.drawable.status, DestinationScreen.StatusList),
    PROFILE(R.drawable.profile, DestinationScreen.Profile)
}

@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, )
            )
            .padding(vertical = 8.dp)
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        for (item in BottomNavigationItem.values()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navigateTo(navController, item.navDestination.route)
                    }
                    .padding(vertical = 4.dp) ,

                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = null,
                    modifier = Modifier.size(if (item == selectedItem) 48.dp else 40.dp),
                    colorFilter = if (item == selectedItem)
                        ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                    else
                        ColorFilter.tint(Color.Gray)
                )
                if (item == selectedItem) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}
