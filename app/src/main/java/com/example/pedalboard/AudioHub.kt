package com.example.pedalboard

import com.example.pedalboard.filtering.baseFilters.LiveBass
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException
import android.media.audiofx.PresetReverb
import androidx.core.net.toUri
import com.example.pedalboard.filtering.Filter
import com.example.pedalboard.filtering.baseFilters.DigitalFilter

private const val TAG = "AudioHub"

class AudioHub private constructor(context: Context)
{

    val recorder: AudioRecorder = AudioRecorder()
    val player: AudioPlayer = AudioPlayer(context)

    private lateinit var sampleFile: File
    private val tempFile: File = File("${context.cacheDir.path}/temp.3gp")

    fun setFile(sampleFile: File) {
        this.sampleFile = sampleFile
        FilesInator.copyFile(sampleFile, tempFile)
        player.setSourceFile(tempFile)
        recorder.setDestinationFile(tempFile)
    }

    fun setFile(path: String) {
        setFile(File(path))
    }

    fun getPlayerSession(): Int {
        return player.getSessionId()
    }

    fun writeFromCache() {
        FilesInator.apply {
            copyFile(tempFile, sampleFile)
        }
    }

    fun killAll() {
        player.kill()
        recorder.kill()
    }

    fun play(listener: () -> Unit) {
        Log.d(TAG, "Playing unfiltered")
        player.setOnCompletionListener(listener)
        player.play()
    }

    fun playFiltered(filter: DigitalFilter, listener: () -> Unit) {
        filter.setSession(player.getSessionId())
        Log.d(TAG, "Playing filtered for filter ${filter}")
        player.setOnCompletionListener { listener(); filter.close() }
        player.play()
    }

    fun playFiltered(filterCreator: ((Int) -> DigitalFilter), listener: () -> Unit) {
        val filter = filterCreator(player.getSessionId())
        Log.d(TAG, "Playing filtered for filter ${filter}")
        player.setOnCompletionListener { listener(); filter.close() }
        player.play()
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

class AudioPlayer (private val context: Context) {
    private var player: MediaPlayer? = null
    private var file: File? = null

    init {
        player = MediaPlayer()
    }

    fun setSourceFile(srcFile: File) {
        file = srcFile
    }

    fun getSessionId(): Int {
        if (player == null) {
            player = MediaPlayer().apply { prepare() }
        }
        return player!!.audioSessionId
    }

    fun play() {
        checkNotNull(player) {"Player not created"}
            .start()
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        checkNotNull(player) {"Player not created"}
            .setOnCompletionListener {reset(); listener()}
    }

    fun close() {
        checkNotNull(player) {"Player not created"}
            .release()
        player = null
    }

    fun reset() {
        checkNotNull(player) {"Player not created"}
            .release()
        player = MediaPlayer()
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