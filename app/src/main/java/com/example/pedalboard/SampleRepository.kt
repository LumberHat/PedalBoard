package edu.appstate.cs.sample

import android.content.Context
import androidx.room.Room
import com.example.pedalboard.Sample
import com.example.pedalboard.database.SamplesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "SampleRepository"
private const val DATABASE_NAME = "sample-database"
class SampleRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {
    private val database: SamplesDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            SamplesDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    fun getSamples(): Flow<List<Sample>> = database.samplesDao().getSamples()

    suspend fun getSample(id: UUID): Sample = database.samplesDao().getSample(id)

    fun updateSample(sample: Sample) {
        coroutineScope.launch {
            database.samplesDao().updateSample(sample)
        }
    }

    suspend fun addSample(sample: Sample) {
        database.samplesDao().addSample(sample)
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