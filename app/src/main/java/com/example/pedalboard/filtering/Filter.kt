package com.example.pedalboard.filtering

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Filter(
    @PrimaryKey val id: UUID,
    var title: String,
    var description: String,
    var filePath: String
)
