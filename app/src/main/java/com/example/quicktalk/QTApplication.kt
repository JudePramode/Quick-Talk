package com.example.quicktalk

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QTApplication : Application(){

    companion object {
        lateinit var instance: QTApplication
            private set
    }






    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        instance = this

    }

}

