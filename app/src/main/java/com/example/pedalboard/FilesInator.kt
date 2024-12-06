package com.example.pedalboard

import android.util.Log
import com.example.pedalboard.filtering.Filter
import com.example.pedalboard.filtering.baseFilters.DigitalFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.FileReader
import java.io.FileWriter
import com.google.gson.Gson

private const val TAG = "FilesInator"
object FilesInator {
    private val coroutineScope: CoroutineScope = GlobalScope

    fun copyFile(src: File, dst: File) {
        coroutineScope.launch {
            try {
                src.copyTo(dst, true)
            } catch (e: IOException) {
                Log.d(TAG, "Error occurred during file copy: $e")
            } catch (e: NoSuchFileException) {
                Log.d(TAG, "Source file not found: $e")
            }
        }
    }

    fun copyFile(src: String, dst: String) {
        copyFile(File(src), File(dst))
    }

    fun deleteFile(file: File) {
        file.delete()
    }
}