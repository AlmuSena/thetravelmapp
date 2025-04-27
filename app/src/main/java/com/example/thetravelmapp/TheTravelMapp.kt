package com.example.thetravelmapp

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TheTravelMapp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)


    }

    companion object {
        // This lets us access the container from composables
        lateinit var instance: TheTravelMapp
            private set
    }

    init {
        instance = this
    }
}