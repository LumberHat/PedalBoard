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

private const val TAG = "AUDIO_HUB"
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
        //player.setSource(sample.id.toString() + ".3gp")
    }

    fun getPlayerSession(): Int {
        return player.sessionId
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

    fun play(listener: (p: MediaPlayer) -> Unit) {
        player.onCompleteListener = listener
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

val statusMap: Map<Int, String> = mapOf(0 to "gone", 1 to "idle", 2 to "initialized", 3 to "prepared",
                                        4 to "preparing", 5 to "started", 6 to "stopped")

class AudioPlayer (
    private val context: Context,
    private var source: String)
{
    private var status = 0
    var sessionId = -1
    private var _onCompleteListener: ((p: MediaPlayer) -> Unit) = {}
    var onCompleteListener
        get() = _onCompleteListener
        set(listener) {
            _onCompleteListener = {p -> stop(); reset(); listener(p)}
        }

    private lateinit var player: MediaPlayer
    private var inputStream: FileInputStream = context.openFileInput(source)

    init {
        var fileURI: Uri = File(context.filesDir, source).toUri()
        setUp(inputStream.fd)
    }

    private fun setUp(fd: FileDescriptor) {
        if (status == 0) {
            Log.d(TAG, "setUp: Creating new MediaPlayer")
            player = MediaPlayer()
            status = 1
        }
        reset()
        setDataSource(fd)
        prepare()

        sessionId = player.audioSessionId
    }

    fun play() {
        player.setOnCompletionListener(onCompleteListener)
        start()
    }

    private fun reset() {
        if (status != 0) {
            Log.d(TAG, "reset")
            player.reset()
            status = 1 //idle
        } else Log.e(TAG, "reset: CANCELED status: ${statusMap[status]}")
        
    }

    private fun setDataSource(fd: FileDescriptor) {
        if (status == 1) {
            Log.d(TAG, "setDataSource: $fd")
            player.setDataSource(fd)
            status = 2
        } else Log.e(TAG, "setDataSource: CANCELED status: ${statusMap[status]}")
    }
    //my 2 == their 1
    private fun prepare() {
        if (status != 2) {
            Log.d(TAG, "prepare")
            player.prepare()
            status = 3
        } else Log.e(TAG, "prepare: CANCELED status: ${statusMap[status]}")
    }

    private fun start() {
        if (status == 3) {
            Log.d(TAG, "start")
            player.start()
            status = 5
        } else Log.e(TAG, "start: CANCELED status: ${statusMap[status]}")
    }

    private fun stop() {
        if (status in 2..5 && status != 4) {
            player.stop()
            status = 6
        }  else Log.e(TAG, "stop: CANCELED status: ${statusMap[status]}")
    }

    private fun release() {
        player.release()
        status = 0
    }

    fun close() {
        player.release()
    }

    fun kill() {
        release()
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