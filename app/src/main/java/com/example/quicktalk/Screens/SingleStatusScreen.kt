package com.example.quicktalk.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quicktalk.CommonImage
import com.example.quicktalk.QTViewModel

enum class State{
    INITIAL, ACTIVE, COMPLETED
}



@Composable
fun SingleStatusScreen(navController: NavHostController, viewModel: QTViewModel, userId: String) {
    val statuses = viewModel.status.value.filter { it.user.userId == userId }
    if (statuses.isNotEmpty()) {
        val currentStatus = remember { mutableStateOf(0) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CommonImage(
                data = statuses[currentStatus.value].imageUrl,
                modifier = Modifier.fillMaxSize(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                statuses.forEachIndexed { index, _ ->
                    CustomProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .padding(1.dp),
                        state = when {
                            currentStatus.value < index -> State.INITIAL
                            currentStatus.value == index -> State.ACTIVE
                            else -> State.COMPLETED
                        }
                    ) {
                        if (currentStatus.value < statuses.size - 1) currentStatus.value++ else navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun CustomProgressIndicator(modifier: Modifier, state: State,onComplete:()->Unit){
    var progress=if (state==State.INITIAL) 0f else 1F

    if (state==State.ACTIVE){
        val toggleState = remember {

            mutableStateOf(false)
        }
        LaunchedEffect(toggleState) {
            toggleState.value=true
        }
        val p: Float by animateFloatAsState(if (toggleState.value) 1f else 0f, animationSpec = tween(5000), finishedListener = {onComplete.invoke()})
        progress=p


    }

    LinearProgressIndicator(modifier= Modifier.fillMaxWidth().padding(top = 25.dp), color = Color.Green, progress = progress)

}