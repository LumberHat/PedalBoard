package com.example.pedalboard.sampling

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.pedalboard.FilesInator
import com.example.pedalboard.sampling.database.SamplesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


private const val TAG = "SampleRepository"
private const val DATABASE_NAME = "sample-database"
class SampleRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {
    //no clue why I can't just reference context directly from the constructor
    //It causes issues in addSample
    private val context = context
    private val database: SamplesDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            SamplesDatabase::class.java,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    fun getSamples(): Flow<List<Sample>> = database.samplesDao().getSamples()

    suspend fun getSample(id: UUID): Sample = database.samplesDao().getSample(id)

    fun updateSample(sample: Sample) {
        coroutineScope.launch {
            database.samplesDao().updateSample(sample)
        }
    }

    suspend fun addSample(sample: Sample) {
        Log.d(TAG, "Adding Sample with ID: ${sample.id}")
        database.samplesDao().addSample(sample)
    }

    suspend fun duplicateSample(sample: Sample) {
        val path = context.getDir("samples", 0)?.absolutePath
        val newId = UUID.randomUUID()
        val newSample = Sample(
            id = newId,
            title = sample.title + "-copy",
            description = sample.description
        )
        database.samplesDao().addSample(newSample)
    }

    fun deleteSample(sample: Sample) {
        coroutineScope.launch {
            database.samplesDao().deleteSample(sample)
        }
    }

    companion object {
        private var INSTANCE: SampleRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = SampleRepository(context)
            }
        }

        fun get(): SampleRepository {
            return INSTANCE
                ?: throw IllegalStateException("SampleRepository must be initialized")
        }
    }
}