package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class PaulApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("PaulApplication", "FirebaseApp initialized successfully")
        } catch (e: Exception) {
            Log.e("PaulApplication", "FirebaseApp initialization error: ${e.message}", e)
        }
    }
}
