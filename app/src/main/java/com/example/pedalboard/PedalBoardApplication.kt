package com.example.pedalboard

import android.app.Application
import android.util.Log
import edu.appstate.cs.sample.SampleRepository

private const val TAG: String = "PedalBoardApplication"

class PedalBoardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SampleRepository.initialize(this)
        Log.d(TAG, "Created")
    }
}