package com.example.kotlin

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.kotlin.config.AppConfig


class AudioRecorder {
    var isRecording = false
        private set
    var audioRecord: AudioRecord? = null
    fun start(activity: Activity) {
        try {
            if (isRecording) {
                return
            }
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1
                )
                return
            }
            audioRecord = AudioRecord(
                AppConfig.AUDIO_SOURCE,
                AppConfig.SAMPLE_RATE,
                AppConfig.CHANNEL,
                AppConfig.ENCODING,
                AppConfig.SIZE_IN_BYTES
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(javaClass.simpleName, "AudioRecord is not initialized")
                return
            }
            audioRecord?.startRecording()
            isRecording = true
        } catch (err: Exception) {
            Log.e(javaClass.simpleName, "Error starting audio recording: ${err.localizedMessage}")
        }
    }
    fun stop() {
        if (!isRecording) return
        try {
            audioRecord?.stop()
            isRecording = false
        } catch (err: Exception) {
            Log.e(javaClass.simpleName, "Error stopping audio recording: ${err.localizedMessage}")
        }
    }
}
