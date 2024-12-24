package com.example.quicktalk


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.quicktalk.data.Event
import com.example.quicktalk.data.USER_NODE
import com.example.quicktalk.data.UserData


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class QTViewModel @Inject constructor(
    val auth:FirebaseAuth,
    var db: FirebaseFirestore

) : ViewModel() {
init {

}
    var inProgress = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)



    fun signUp(name:String, number:String, email:String, password:String) {
        inProgress.value = true
        auth.createUserWithEmailAndPassword(email,password) .addOnCompleteListener{
            if(it.isSuccessful){
                signIn.value = true
                createOrUpdateProfile(name, number)

            }else{
            handleException(it.exception, customMessage = "Sign Up failed")
            }

        }

    }
    fun createOrUpdateProfile(name: String?=null, number: String?=null, imageurl:String?=null){

        val uid = auth.currentUser?.uid
        val updatedUserData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageurl ?: userData.value?.imageUrl
        )
        uid?.let {
            inProgress.value=true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (it.exists()){
                   // update dUser Data

                }else{
                    db.collection(USER_NODE).document(uid).set(userData)
                    inProgress.value=false
                    getUserData(uid)

                }


            }
                .addOnFailureListener{
                    handleException(it, "Cannot Retrieve User")
                }

        }

    }

    private fun getUserData(uid:String) {
       inProgress.value=true
        db.collection(USER_NODE).document(uid).addSnapshotListener{
            value , error->
            if (error!=null){
                handleException(error,"Cannot retrieve User")
            }
            if (value !=null){
                var user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
            }
        }
    }


    fun handleException(exception: Exception?=null, customMessage:String=""){
        Log.e("QuickTalkApp", "Quick Talk Exception ", exception )
        exception?.printStackTrace()
        val errorMsg=exception?.localizedMessage?:""
        val message=if (customMessage.isNullOrEmpty())errorMsg else customMessage

        eventMutableState.value=Event(message)
        inProgress.value = false
    }


}

