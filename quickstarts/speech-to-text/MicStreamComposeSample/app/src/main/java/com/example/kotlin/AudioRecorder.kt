package com.example.kotlin

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.kotlin.config.AppConfig


class AudioRecorder {
    // 録音中フラグ
    private var isRecording = false
    // AudioRecordインスタンス
    var audioRecord : AudioRecord? = null

    // 録音開始
    fun recordStart(activity: Activity) {
        try{
            if (isRecording) {
                isRecording = false
                return
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                return
            }
            // AudioRecord作成
            audioRecord = AudioRecord(AppConfig.AUDIO_SOURCE, AppConfig.SAMPLE_RATE, AppConfig.CHANNEL, AppConfig.ENCODING, AppConfig.SIZE_IN_BYTES)
            // 録音開始
            audioRecord!!.startRecording()
            isRecording = true
        } catch (err:Exception){
            Log.e(javaClass.simpleName, err.toString())
        }
    }

    fun recordStop(){
        isRecording = false
    }

    fun release(){
        if (audioRecord != null) {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
        }
    }

    // 録音状態チェック
    // true:録音中 false:録音停止状態
    fun isRecording(): Boolean {
        return isRecording
    }
}
