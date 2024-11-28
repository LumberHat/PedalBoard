package com.example.pedalboard.sampling.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pedalboard.sampling.Sample
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SamplesDao {
    @Query("SELECT * FROM sample")
    fun getSamples(): Flow<List<Sample>>

    @Query("SELECT * FROM sample WHERE id=(:id)")
    suspend fun getSample(id: UUID): Sample

    @Update
    suspend fun updateSample(sample: Sample)

    @Insert
    suspend fun addSample(sample: Sample)

    @Delete
    suspend fun deleteSample(sample: Sample)}