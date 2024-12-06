package com.example.pedalboard.filtering

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.pedalboard.FilesInator
import com.example.pedalboard.filtering.baseFilters.FilterData
import com.example.pedalboard.filtering.database.FiltersDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


private const val TAG = "FilterRepository"
private const val DATABASE_NAME = "filter-database"
class FilterRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {
    //no clue why I can't just reference context directly from the constructor
    //It causes issues in addFilter
    private val context = context
    private val database: FiltersDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            FiltersDatabase::class.java,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    fun getFilters(): Flow<List<Filter>> = database.filtersDao().getFilters()

    suspend fun getFilter(id: UUID): Filter = database.filtersDao().getFilter(id)

    fun updateFilter(filter: Filter) {
        coroutineScope.launch {
            database.filtersDao().updateFilter(filter)
        }
    }

    suspend fun addFilter(filter: Filter) {
        Log.d(TAG, "Adding Filter with ID: ${filter.id}")
        database.filtersDao().addFilter(filter)
    }

    suspend fun duplicateFilter(filter: Filter) {
        val path = context.getDir("filters", 0)?.absolutePath
        val newId = UUID.randomUUID()
        val newFilter = Filter(
            id = newId,
            title = filter.title + "-copy",
            description = filter.description,
            config = FilterData()
        )
        database.filtersDao().addFilter(newFilter)
    }

    fun deleteFilter(filter: Filter) {
        coroutineScope.launch {
            database.filtersDao().deleteFilter(filter)
        }
    }

    companion object {
        private var INSTANCE: FilterRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = FilterRepository(context)
            }
        }

        fun get(): FilterRepository {
            return INSTANCE
                ?: throw IllegalStateException("FilterRepository must be initialized")
        }
    }
}