package com.example.pedalboard.filtering.baseFilters

import java.io.File

abstract class DigitalFilter {
    abstract fun apply(audioFile: File) : File
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