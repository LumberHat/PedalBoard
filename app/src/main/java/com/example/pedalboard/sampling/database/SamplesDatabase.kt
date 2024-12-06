package com.example.pedalboard.sampling.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pedalboard.sampling.Sample

@Database(entities = [Sample::class], version = 3)
abstract class SamplesDatabase : RoomDatabase() {
    abstract fun samplesDao(): SamplesDao
}