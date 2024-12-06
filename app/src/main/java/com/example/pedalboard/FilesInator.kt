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
import android.app.Activity
import android.content.Context
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createFile

private const val TAG = "FilesInator"
class FilesInator(private val context: Context) {
    private val coroutineScope: CoroutineScope = GlobalScope
    val directory = context.filesDir
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

    fun newFile(name: String) {
        val f = FileOutputStream(name)
        f.write(byteArrayOf())
        f.close()
    }

    fun writeToFile(name: String, data: ByteArray) {
        val f = FileOutputStream(name)
        f.write(data)
        f.close()
    }

    fun copyFile(src: String, dst: String) {
        copyFile(File(src), File(dst))
    }

    fun deleteFile(file: File) {
        file.delete()
    }

    companion object {
        private var INSTANCE: FilesInator? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = FilesInator(context)
            }
        }

        fun get(): FilesInator {
            return INSTANCE
                ?: throw IllegalStateException("FileInator must be initialized")
        }
    }
}