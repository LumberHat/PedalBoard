package com.example.pedalboard

import com.example.pedalboard.filtering.baseFilters.LiveBass
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException
import android.media.audiofx.PresetReverb
import android.net.Uri
import androidx.core.net.toUri
import com.example.pedalboard.filtering.Filter
import com.example.pedalboard.filtering.baseFilters.DigitalFilter
import com.example.pedalboard.sampling.Sample
import kotlinx.coroutines.flow.merge
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.log

private const val TAG = "AudioHub"

class AudioHub private constructor(val context: Context)
{
    val recorder: AudioRecorder
    val player: AudioPlayer
    var _sample: Sample? = null
    var SSIN: FileInputStream? = null
    var SSOUT: FileOutputStream? = null

    var srcStream: FileInputStream
    var dstStream: FileOutputStream
    init {
        context.openFileOutput("temp.3gp", Context.MODE_PRIVATE).apply { write(byteArrayOf()); close() }
        srcStream = context.openFileInput("temp.3gp")
        dstStream = context.openFileOutput("temp.3gp", Context.MODE_PRIVATE)
        val srcfd = srcStream.fd
        val dstfd = dstStream.fd
        Log.d(TAG, "src: $srcfd valid: ${srcfd.valid()}")
        Log.d(TAG, "src: $dstfd valid: ${dstfd.valid()}")
        recorder = AudioRecorder(context)
        player = AudioPlayer(context, "temp.3gp")
    }

    fun setSample(sample: Sample) {
        _sample = sample
        context.openFileOutput(sample.id.toString() + ".3gp", Context.MODE_PRIVATE).close()
        player.setSource(sample.id.toString() + ".3gp")
    }

    fun getPlayerSession(): Int {
        return player.getSessionId()
    }

    fun writeFromCache() {
        SSOUT.use { fileOut ->
            if (fileOut != null) {
                srcStream.copyTo(fileOut)
            }
        }
    }

    fun killAll() {
        srcStream.close()
        dstStream.close()

        player.kill()
        recorder.kill()
    }

    fun play(listener: () -> Unit) {
        player.setOnCompletionListener(listener)
        player.play()
    }

    fun toggleRecording() : Boolean {
        val b = recorder.toggleRecording()
        if (!b) {
            //player.setSource("temp.3gp")
        }
        return b
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

class AudioPlayer (
    private val context: Context,
    private var source: String) {

    lateinit var player: MediaPlayer
    var listener: (() -> Unit) = {}
    private var inputStream: FileInputStream

    init {
        inputStream = context.openFileInput(source)
        var f = File(context.filesDir, source).toUri()
        Log.d(TAG, "THIS SHOULD APPEAR ONLY ONCE $f  ${f.path}")
        val myUri: Uri = f
        player = MediaPlayer()
    }

    fun setSource(src: String) {
        source = src
        val t = player.audioSessionId
        player.release()
    }

    fun getSessionId(): Int {
        return player.audioSessionId
    }

    fun play() {
        player.start()
    }

    fun setUp() {

    }

    fun setOnCompletionListener(listener: () -> Unit) {
        Log.d(TAG, "setOnCompletionListener")
        this.listener = listener
    }

    fun close() {
        player.release()
    }

    fun reset() {
        Log.d(TAG, "reset: ")
        inputStream?.close()
        player.release()
        player = MediaPlayer()
    }

    fun kill() {
//        player.apply {
//            stop()
//            release()
//        }
    }
}

class AudioRecorder(private val context: Context) {

    private var _isRecording: Boolean = false
    val isRecording
        get() = _isRecording

    private var recorder: MediaRecorder? = null
    private var dstStream: FileOutputStream? = null
    private val source = MediaRecorder.AudioSource.MIC
    private val format = MediaRecorder.OutputFormat.THREE_GPP
    private val encoder = MediaRecorder.AudioEncoder.AMR_NB

    @Throws(IOException::class)
    fun toggleRecording() : Boolean {
        if (isRecording) {
            stopRecording()
            dstStream?.close()
        } else {
            dstStream = context.openFileOutput("temp.3gp", Context.MODE_PRIVATE)
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
            setOutputFile(dstStream?.fd)
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