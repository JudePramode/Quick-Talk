package com.example.quicktalk.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.quicktalk.CommonDivider
import com.example.quicktalk.CommonImage
import com.example.quicktalk.QTViewModel
import com.example.quicktalk.R
import org.w3c.dom.Text

@Composable
fun SingleChatScreen(navController: NavController,viewModel: QTViewModel, chatId:String) {

    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val onSendReply={
        viewModel.onSendReply(chatId,reply)
        reply = ""
    }

    val myUser = viewModel.userData.value
    var currentChat = viewModel.chats.value.first{it.chatId==chatId}
    val chatUser = if(myUser?.userId==currentChat.user1.userId) currentChat.user2 else currentChat.user1


    LaunchedEffect(key1 = Unit) {


    }
    BackHandler {

    }

    Column {

        ChatHeader(name=chatUser.name?:"", imageUrl =chatUser.imageUrl?:"" ) {
            navController.popBackStack()




        }

        ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)
    }
}


@Composable
fun ChatHeader(name:String, imageUrl:String, onBackClicked:()->Unit){
    Row (modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(top = 30.dp),
        verticalAlignment = Alignment.CenterVertically){
        Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.clickable {
            onBackClicked.invoke()
        }.padding(8.dp))
        CommonImage(data = imageUrl, modifier = Modifier.padding(8.dp).size(50.dp).clip(CircleShape)

        )
        Text(text=name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = reply,
                onValueChange = onReplyChange,
                placeholder = { Text(text = "Type a message...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .padding(start = 15.dp)
                    .padding(end = 8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            Button(
                onClick = onSendReply,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(40.dp).width(90.dp).padding(end = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.send), // Replace with your drawable resource name
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
