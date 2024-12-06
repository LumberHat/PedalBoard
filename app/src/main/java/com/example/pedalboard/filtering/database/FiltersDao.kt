package com.example.pedalboard.filtering.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pedalboard.filtering.Filter
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FiltersDao {
    @Query("SELECT * FROM filter")
    fun getFilters(): Flow<List<Filter>>

    @Query("SELECT * FROM filter WHERE id=(:id)")
    suspend fun getFilter(id: UUID): Filter

    @Update
    suspend fun updateFilter(filter: Filter)

    @Insert
    suspend fun addFilter(filter: Filter)

    @Delete
    suspend fun deleteFilter(filter: Filter)}