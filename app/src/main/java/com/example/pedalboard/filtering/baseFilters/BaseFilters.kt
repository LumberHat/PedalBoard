package com.example.pedalboard.filtering.baseFilters
import android.media.audiofx.*
import android.media.audiofx.BassBoost
import android.net.rtp.AudioStream
import android.provider.MediaStore.Audio
import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private const val TAG = "BASE_FILTERS"
abstract class DigitalFilter(protected var audioSession: Int) {
    private var filterComponents: List<FilterComponent> = emptyList()
    open val name = "Default"

    fun componentCount(): Int {
        return filterComponents.size - 1
    }

    fun getComponent(index: Int): FilterComponent {
        return filterComponents[index]
    }

    protected fun addComponent(component: FilterComponent) {
        filterComponents += component
    }

    fun getData(): FilterData {
        val data = FilterData()
        data.filterType = name
        data.config = filterComponents
        return data
    }

    open fun setSession(audioSession: Int) {
        Log.d(TAG, "session id: $audioSession")
        this.audioSession = audioSession
    }
    abstract fun close()

    companion object {
        fun fromData(data: FilterData, audioSession: Int): DigitalFilter {
            val filter = bigSwitchStatement(data.filterType)(audioSession)
            filter.filterComponents = data.config
            return filter
        }

        fun bigSwitchStatement(str: String) : ((audioSession: Int) -> DigitalFilter){
            when (str) {
                "Eq" -> return {audioSession -> Eq(audioSession)}
                else -> return {audioSession -> LiveBass(audioSession)}
            }
        }
    }
}

class FilterComponent(
    val label: String,
    private val default: Int,
    private val update: ((Int) -> Unit),
    val min: Int?,
    val max: Int?) {

    private var _value = default
    var value
        get() = _value
        set(v: Int) {
            if (min != null && v < min) {
                Log.d(TAG, "Cant update component. Value $v is less than minimum $min")
            } else if (max != null && v > max) {
                Log.d(TAG, "Cant update component. Value $v is greater than maximum $max")
            } else {
                _value = v
                update(v)
            }
        }
}

class FilterData() {
    var config: List<FilterComponent> = emptyList()
    var filterType: String = ""
}

class LiveBass(audioSession: Int) : DigitalFilter(audioSession) {
    lateinit var effect: BassBoost

    override fun setSession(audioSession: Int) {
        super.setSession(audioSession)
        this.effect = BassBoost(0, audioSession)
        this.effect.setEnabled(true)
        this.effect.setStrength(255)
        Log.d(TAG, "${this.effect.hasControl()}")
    }

    override fun close() {
        this.effect.release()
    }
}

class Eq(audioSession: Int) : DigitalFilter(audioSession) {
    override val name = "Eq"

    var effect: Equalizer
    var bands: Short = -1
    var min: Short = -1
    var max: Short = -1

    init {
        this.effect = Equalizer(0, audioSession)
        bands = this.effect.numberOfBands
        val (mint, maxt) = this.effect.bandLevelRange
        min = mint; max = maxt
        this.effect.setEnabled(true)


        for (band in 0..bands-1) {

            addComponent(FilterComponent("Band: #$band", min.toInt(), {
                value -> this.effect.setBandLevel(band.toShort(), value.toShort())
            }, min.toInt(), max.toInt()))
        }
    }

    override fun close() {
        this.effect.release()
    }
}

//class Distortion: DigitalFilter() {
//
//}
//
//class Delay: DigitalFilter() {
//
//}
//
//class Tremolo: DigitalFilter() {
//
//}
//
//class Reverb: DigitalFilter() {
//
//}
//
//class Pitch: DigitalFilter() {
//
//}