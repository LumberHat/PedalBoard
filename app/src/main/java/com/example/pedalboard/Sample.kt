package com.example.pedalboard

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Sample(
    @PrimaryKey val id: UUID,
    var title: String,
    var filePath: String
)