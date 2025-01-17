package com.example.quicktalk.Screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.quicktalk.CommonDivider
import com.example.quicktalk.CommonProgressBar
import com.example.quicktalk.DestinationScreen
import com.example.quicktalk.QTViewModel
import com.example.quicktalk.R
import com.example.quicktalk.navigateTo


@Composable
fun ProfileScreen(navController: NavHostController, viewModel: QTViewModel) {
    val inProgress = viewModel.inProgress.value
    val userData = viewModel.userData.value
    var name by rememberSaveable {
        mutableStateOf(userData?.name?:"")
    }

    var number by rememberSaveable {
        mutableStateOf(userData?.number?:"")
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 56.dp)
        ) {
            ProfileContent(
                modifier = Modifier.fillMaxWidth(),
                viewModel = viewModel,
                name = name,
                number = number,
                onNameChange = { name=it},
                onNumberChange = { number=it },
                onSave = { viewModel.createOrUpdateProfile(
                    name = name, number = number
                ) },
                onBack = { navigateTo(navController=navController, route = DestinationScreen.ChatList.route) },
                onLogout = { viewModel.logout()
                navigateTo(navController=navController, route = DestinationScreen.Login.route)
                }
            )
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }

        // Progress indicator if loading
        if (inProgress) {
            CommonProgressBar()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier,
    viewModel: QTViewModel,
    name: String,
    number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    val imageUrl = viewModel.userData.value?.imageUrl

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp)
                .padding(8.dp)
                .padding(start = 25.dp)
                .padding(end = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", Modifier.clickable { onBack.invoke() })
            Text(text = "Save", Modifier.clickable { onSave.invoke() })
        }
        CommonDivider()

        ProfileImage(imageUrl = imageUrl, viewModel = viewModel)
        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 30.dp)
                    .padding(top = 40.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Number", modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp))
            TextField(
                value = number,
                onValueChange = onNumberChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 30.dp)
            )
        }

        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LogOut",
                modifier = Modifier.clickable { onLogout.invoke() } , Color.Red

            )
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, viewModel: QTViewModel) {

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadProfileImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = viewModel.saveBitmapToUri(bitmap)
            uri?.let { viewModel.uploadProfileImage(it) }
        }
    }

    // Launcher to request camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch() // Launch the camera if permission is granted
        } else {
            Log.e("ProfileImage", "Camera permission denied.")
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { galleryLauncher.launch("image/*") }
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "")
                }
            }
            Text(
                text = "Change Profile Picture",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Show progress indicator while uploading
        if (viewModel.inProgress.value) {
            CommonProgressBar()
        }
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .padding(bottom = 12.dp)
    ) {
        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
            Icon(
                painter = painterResource(id = R.drawable.gallery),
                contentDescription = "Open Gallery",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp).fillMaxSize()

            )
        }
        IconButton(onClick = {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) // Request camera permission
        }) {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Open Camera",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp).fillMaxSize()
            )
        }
    }
}
