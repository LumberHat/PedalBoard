package com.example.pedalboard.filtering

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pedalboard.filtering.baseFilters.FilterData
import java.util.UUID

@Entity
data class Filter(
    @PrimaryKey val id: UUID,
    var title: String,
    var description: String,
    var config: FilterData
)
