package com.example.pedalboard.filtering.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.pedalboard.filtering.Filter

@Database(entities = [Filter::class], version = 3)
@TypeConverters(FiltersTypeConverters::class)
abstract class FiltersDatabase : RoomDatabase() {
    abstract fun filtersDao(): FiltersDao
}