package com.example.quicktalk


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.quicktalk.data.CHATS
import com.example.quicktalk.data.ChatData
import com.example.quicktalk.data.ChatUser
import com.example.quicktalk.data.Event
import com.example.quicktalk.data.MESSAGE
import com.example.quicktalk.data.Message
import com.example.quicktalk.data.STATUS
import com.example.quicktalk.data.Status
import com.example.quicktalk.data.USER_NODE
import com.example.quicktalk.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
    var photoUri: Uri? = null


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
                    createOrUpdateProfile(imageurl = downloadUri.toString())
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
                    populateStatuses()
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

    fun uploadStatus(uri: Uri) {
            uploadImage(uri){
                createStatus(it.toString())
            }
    }

    fun createStatus(imageurl:String){
        val newStatus=Status(
           ChatUser(
               userData.value?.userId,
               userData.value?.name,
               userData.value?.imageUrl,
               userData.value?.number,
           ) ,
            imageurl,
            System.currentTimeMillis()

        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatuses() {
        val timeDelta = 24L * 60 * 60 * 1000 // 24 hours in milliseconds
        val cutoff = System.currentTimeMillis() - timeDelta

        inProgressStatus.value = true

        // Fetch all chats involving the current user
        db.collection(CHATS)
            .where(
                Filter.or(
                    Filter.equalTo("user1.userId", userData.value?.userId),
                    Filter.equalTo("user2.userId", userData.value?.userId)
                )
            )
            .get()
            .addOnSuccessListener { chatSnapshot ->
                val currentConnections = mutableSetOf(userData.value?.userId)
                val chats = chatSnapshot.toObjects<ChatData>()
                chats.forEach { chat ->
                    currentConnections.add(chat.user1.userId)
                    currentConnections.add(chat.user2.userId)
                }

                // Fetch statuses from users in currentConnections
                db.collection(STATUS)
                    .whereGreaterThan("timestamp", cutoff)
                    .whereIn("user.userId", currentConnections.toList())
                    .addSnapshotListener { statusSnapshot, error ->
                        if (error != null) {
                            handleException(error)
                            inProgressStatus.value = false
                            return@addSnapshotListener
                        }

                        statusSnapshot?.let {
                            status.value = it.toObjects<Status>()
                        }
                        inProgressStatus.value = false
                    }
            }
            .addOnFailureListener { error ->
                handleException(error)
                inProgressStatus.value = false
            }
    }

    fun deleteChat(chatId: String) {
        db.collection(CHATS).document(chatId).delete()
            .addOnSuccessListener {
                Log.d("QTViewModel", "Chat deleted: $chatId")
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Failed to delete chat")
            }
    }

    fun launchCamera(context: Context): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            val photoFile: File? = try {
                File.createTempFile(
                    "IMG_${System.currentTimeMillis()}_",
                    ".jpg",
                    context.cacheDir
                )
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            photoFile?.let {
                photoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            return takePictureIntent
        }
        return null
    }


    fun saveBitmapToUri(bitmap: Bitmap): Uri? {
        val context = QTApplication.instance.applicationContext
        val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
        return try {
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}