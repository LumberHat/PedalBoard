package com.example.pedalboard

import android.app.Application
import android.util.Log
import com.example.pedalboard.filtering.FilterRepository
import com.example.pedalboard.sampling.SampleRepository

private const val TAG: String = "PedalBoardApplication"

class PedalBoardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SampleRepository.initialize(this)
        FilterRepository.initialize(this)
        AudioHub.initialize(this)
        Log.d(TAG, "Created")
    }
}