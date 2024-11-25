package com.example.pedalboard.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pedalboard.Sample


@Database(entities = [Sample::class], version = 1)
abstract class SamplesDatabase : RoomDatabase() {
    abstract fun samplesDao(): SamplesDao
}