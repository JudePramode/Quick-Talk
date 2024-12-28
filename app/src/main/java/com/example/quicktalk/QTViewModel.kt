package com.example.quicktalk


import android.net.Uri

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import coil3.request.ImageRequest
import com.example.quicktalk.data.CHATS
import com.example.quicktalk.data.ChatData
import com.example.quicktalk.data.ChatUser
import com.example.quicktalk.data.Event
import com.example.quicktalk.data.MESSAGE
import com.example.quicktalk.data.USER_NODE
import com.example.quicktalk.data.UserData
import com.example.quicktalk.data.Message
import com.example.quicktalk.data.Status


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class QTViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    var inProgress = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages= mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration?=null

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId:String){
        inProgressChatMessage.value = true
        currentChatMessageListener= db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener{value,error->
                if (error!=null){
                    handleException(error)

                }
                if (value!=null){
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessage.value=false
                }

            }
    }

    fun depopulateMessage(){
        chatMessages.value = listOf()
        currentChatMessageListener = null



    }





    fun populateChats(){
        inProcessChats.value=true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId",userData.value?.userId),
                Filter.equalTo("user2.userId",userData.value?.userId),
            )
        ).addSnapshotListener{
                value,error->
            if (error!=null){
                handleException(error)

            }
            if (value!=null){
                chats.value=value.documents.mapNotNull {
                    it.toObject<ChatData>()

                }
                inProgress.value=false
            }

        }

    }



    fun onSendReply(chatId:String, message: String){
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)

    }







    fun signUp(name: String, number: String, email: String, password: String) {
        if (name.isEmpty() || number.isEmpty() || email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill all fields")
            return
        }

        inProgress.value = true
        db.collection(USER_NODE).whereEqualTo("number", number).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            signIn.value = true
                            createOrUpdateProfile(name, number)
                        } else {
                            handleException(task.exception, customMessage = "Sign Up failed")
                        }
                    }
                } else {
                    handleException(customMessage = "Number already exists")
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Failed to verify number")
                inProgress.value = false
            }
    }

    fun loginIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill all fields")
            return
        }

        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(task.exception, customMessage = "Login failed")
                }
            }
    }

    fun uploadProfileImage(uri: Uri) {
        inProgress.value = true
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("uploadProfileImage", "Image uploaded: $downloadUri")
                    createOrUpdateProfile(imageurl = downloadUri.toString()) // Update user profile with URL
                    inProgress.value = false
                }.addOnFailureListener { exception ->
                    Log.e("uploadProfileImage", "Error getting download URL", exception)
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("uploadProfileImage", "Error uploading image", exception)
                inProgress.value = false
            }
    }





    fun uploadImage(uri: Uri, onSuccess:(Uri)->Unit){
        inProgress.value=true
        val storageRef = storage.reference
        val uuid= UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result=it.metadata?.reference?.downloadUrl

            result?.addOnSuccessListener(onSuccess)
            inProgress.value=false

        }
            .addOnFailureListener{
                handleException(it)
            }

    }

    fun createOrUpdateProfile(name: String? = null, number: String? = null, imageurl: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val updatedUserData = UserData(
            userId = uid,
            name = name ?: userData.value?.name ?: "Unknown",
            number = number ?: userData.value?.number ?: "Unknown",
            imageUrl = imageurl ?: userData.value?.imageUrl ?: ""
        )

        db.collection(USER_NODE).document(uid)
            .set(updatedUserData, SetOptions.merge())
            .addOnSuccessListener {
                inProgress.value = false
                getUserData(uid)
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Failed to create or update profile")
            }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USER_NODE).document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                    populateChats()
                }

            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot retrieve User")
            }
    }




    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("QuickTalkApp", "Quick Talk Exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"

        eventMutableState.value = Event(message)
        inProgress.value = false
    }

    fun logout() {
        auth.signOut()
        signIn.value=false
        userData.value=null
        depopulateMessage()
        currentChatMessageListener=null
        eventMutableState.value=Event("Logged Out")
    }

    fun onAddChat(number: String) {

        if (number.isEmpty() or ! number.isDigitsOnly()){
            handleException(customMessage = "Number must be contain digits only!")
        }else{
            db.collection(CHATS).where(Filter.or(

                Filter.and(
                    Filter.equalTo("user1.number",number),
                    Filter.equalTo("user2.number",userData.value?.number)
                ),
                Filter.and(
                    Filter.equalTo("user1.number",userData.value?.number),
                    Filter.equalTo("user2.number",number)
                )


            )).get().addOnSuccessListener {
                if (it.isEmpty){
                    db.collection(USER_NODE).whereEqualTo("number",number).get().addOnSuccessListener {
                        if (it.isEmpty){
                            handleException(customMessage = "Number not found")
                        }else{
                            val chatPartner = it.toObjects<UserData>()[0]
                            val id=db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                ChatUser(userData.value?.userId, userData.value?.name,
                                    userData.value?.imageUrl, userData.value?.number
                                ),
                                ChatUser(chatPartner.userId, chatPartner.name,chatPartner.imageUrl,chatPartner.number)
                            )
                            db.collection(CHATS).document(id).set(chat)
                        }
                    }
                        .addOnFailureListener {
                            handleException(it)
                        }

                }else{
                    handleException(customMessage = "Chat already exists")
                }
            }
        }

    }


}
