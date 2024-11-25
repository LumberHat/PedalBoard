package com.example.pedalboard

import android.app.Application
import android.util.Log

private const val TAG: String = "PedalBoardApplication"

class PedalBoardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Created")
    }
}