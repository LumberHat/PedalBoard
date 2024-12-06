package com.example.pedalboard.filtering.database

import androidx.room.TypeConverter
import com.example.pedalboard.filtering.baseFilters.DigitalFilter
import com.example.pedalboard.filtering.baseFilters.FilterData
import com.google.gson.Gson
import java.io.FileReader

class FiltersTypeConverters {

    @TypeConverter
    fun fromFilterData(data: FilterData): String {
        return Gson().toJson(data, FilterData::class.java)
    }

    @TypeConverter
    fun toFilterData(str: String): FilterData {
        return Gson().fromJson(str, FilterData::class.java)
    }
}