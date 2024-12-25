package com.example.quicktalk


import android.net.Uri
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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class QTViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    var inProgress = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
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
        inProgress.value = true // Indicate progress in the UI

        uploadImage(uri) { downloadUri ->
            createOrUpdateProfile(imageurl = downloadUri.toString())
            inProgress.value = false // Reset progress state
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
                }
                inProgress.value = false
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
        eventMutableState.value=Event("Logged Out")
    }
}

