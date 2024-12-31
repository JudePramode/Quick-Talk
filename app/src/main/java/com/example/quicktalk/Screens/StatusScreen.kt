package com.example.quicktalk.Screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quicktalk.CommonDivider
import com.example.quicktalk.CommonProgressBar
import com.example.quicktalk.CommonRow
import com.example.quicktalk.DestinationScreen
import com.example.quicktalk.QTViewModel
import com.example.quicktalk.TitleText
import com.example.quicktalk.navigateTo

@Composable
fun StatusScreen(navController: NavHostController, viewModel: QTViewModel) {
    val inProgress = viewModel.inProgressStatus.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val statuses = viewModel.status.value
        val userData = viewModel.userData.value

        // Separate statuses
        val myStatuses = statuses.filter { it.user.userId == userData?.userId }
        val otherStatuses = statuses.filter { it.user.userId != userData?.userId }

        // File picker for uploading status
        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.uploadStatus(it) }
        }

        Scaffold(
            floatingActionButton = {
                FAB { launcher.launch("image/*") }
            },
            bottomBar = {
                BottomNavigationMenu(
                    selectedItem = BottomNavigationItem.STATUSLIST,
                    navController = navController
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 16.dp)
            ) {
                TitleText(txt = "Status")

                if (statuses.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "No Statuses Available")
                    }
                } else {
                    if (myStatuses.isNotEmpty()) {
                        CommonRow(imageUrl = myStatuses[0].user.imageUrl, name = myStatuses[0].user.name) {
                            myStatuses[0].user.userId?.let { userId ->
                                navigateTo(navController, DestinationScreen.SingleStatus.createRoute(userId))
                            }
                        }
                        CommonDivider()
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(otherStatuses) { status ->
                            CommonRow(imageUrl = status.user.imageUrl, name = status.user.name) {
                                status.user.userId?.let { userId ->
                                    navigateTo(navController, DestinationScreen.SingleStatus.createRoute(userId))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun FAB(
    onFabClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.White
        )
    }
}

