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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.merge
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.log

private const val TAG = "AUDIO_HUB"
class AudioHub private constructor(val context: Context)
{
    var recorder: AudioRecorder? = null
    var player: AudioPlayer? = null
    var isRecording = false
    var sampleFileName = ""

    init {
        context.openFileOutput("temp.3gp", Context.MODE_PRIVATE).apply { write(byteArrayOf()); close() }

    }

    fun start(sample: Sample) {
        recorder = AudioRecorder(context)
        sampleFileName = sample.id.toString()+".3gp"
        player = AudioPlayer(context, sampleFileName)
    }

    fun getPlayerSession(): Int {
        return player?.sessionId ?: -1
    }

    fun writeFromCache() {

        val dstStream = context.openFileOutput(sampleFileName, Context.MODE_PRIVATE)
        val srcStream = context.openFileInput("temp.3gp")
        dstStream.use { fileOut ->
            if (fileOut != null) {
                srcStream.copyTo(fileOut)
            }
        }
        srcStream.close()
        dstStream.close()
    }

    fun killAll() {
        recorder?.kill()
    }

    fun play(listener: () -> Unit) {
        player?.onCompleteListener = listener
        player?.playRecording()
    }

    fun toggleRecording() : Boolean {

        isRecording = recorder?.toggleRecording() ?: false
        if (!isRecording) player?.sourceName = "temp.3gp"
        return isRecording
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
    var sourceName: String)
{
    private var status = 0
    var sessionId = -1
    var onCompleteListener: (() -> Unit) = {}


    private lateinit var player: MediaPlayer

    fun playRecording() {
        val src = context.openFileInput(sourceName)
        val audioPlayer = MediaPlayer().apply {
            try {
                setDataSource(src.fd)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed in playRecording() for source: $sourceName")
                onCompleteListener()
            }
        }
        audioPlayer.setOnCompletionListener {
            p->onCompleteListener();p.release();src.close()
        }
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