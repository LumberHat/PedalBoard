package com.example.pedalboard

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException

private const val TAG = "AudioHub"

class AudioHub private constructor(context: Context)
{

    val recorder: AudioRecorder = AudioRecorder()
    val player: AudioPlayer = AudioPlayer()

    private lateinit var sampleFile: File
    private val tempFile: File = File("${context.cacheDir.path}/temp.3gp")

    fun setFile(sampleFile: File) {
        this.sampleFile = sampleFile
        FilesInator.copyFile(sampleFile, tempFile)
        player.setSourceFile(tempFile)
        recorder.setDestinationFile(tempFile)
    }

    fun writeFromCache() {
        FilesInator.apply {
            copyFile(tempFile, sampleFile)
            deleteFile(tempFile)
        }
    }

    fun killAll() {
        player.kill()
        recorder.kill()
    }

    companion object {
        private var INSTANCE: AudioHub? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AudioHub(context)
            }
        }

        fun get(): AudioHub {
            return INSTANCE
                ?: throw IllegalStateException("AudioHub must be initialized")
        }
    }
}

class AudioPlayer {
    private var player: MediaPlayer? = null

    private var file: File? = null


    fun setSourceFile(srcFile: File) {
        file = srcFile
    }

    @Throws(IOException::class)
    fun play(listener: (() -> Unit)) {
        player = MediaPlayer().apply {
            try {
                setDataSource(file?.path)
                prepare()
                start()
            } catch (e: IOException) {
                release()
                listener()
                throw(e)
            }
        }

        player?.setOnCompletionListener {
            player?.release()
            listener()
            player = null
        }
    }

    fun kill() {
        player?.apply {
            stop()
            release()
        }
        player = null
    }
}

class AudioRecorder {

    private var _isRecording: Boolean = false
    val isRecording
        get() = _isRecording

    private var recorder: MediaRecorder? = null

    private val source = MediaRecorder.AudioSource.MIC
    private val format = MediaRecorder.OutputFormat.THREE_GPP
    private val encoder = MediaRecorder.AudioEncoder.AMR_NB

    private var file: File? = null

    fun setDestinationFile(dstFile: File) {
        file = dstFile
    }

    @Throws(IOException::class)
    fun toggleRecording() : Boolean {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
        return _isRecording
    }

    @Throws(IOException::class)
    fun startRecording() {
        Log.d(TAG, "Starting Recording")
        recorder = MediaRecorder().apply {
            setAudioSource(source)
            setOutputFormat(format)
            setOutputFile(file)
            setAudioEncoder(encoder)
            try {
                prepare()
            } catch (e: IOException) {
                stopRecording()
                throw(e)
            }
            _isRecording = true
            start()
        }
        Log.d(TAG, "Started Recording")
    }

    @Throws(IOException::class)
    fun stopRecording() {
        _isRecording = false;
        Log.d(TAG, "Stopping Recording")
        recorder?.apply {
            stop()
            release()
        }
        recorder = null;
        Log.d(TAG, "Stopped Recording")
    }

    fun kill() {
        try {
            stopRecording()
        } catch (e: IOException) {
            Log.d(TAG, "Error killing AudioRecorder")
        }
    }
}