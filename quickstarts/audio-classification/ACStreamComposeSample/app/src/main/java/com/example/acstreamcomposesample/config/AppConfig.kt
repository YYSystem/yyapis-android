package com.example.acstreamcomposesample.config

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.acstreamcomposesample.BuildConfig

class AppConfig {

    companion object {
        // 接続先設定
        val yyapisApiKey = BuildConfig.yyapisApiKey
        val yyapisEndpoint = BuildConfig.yyapisEndpoint
        val yyapisPort = BuildConfig.yyapisPort.toInt()
        val yyapisSsl = BuildConfig.yyapisSsl.toBoolean()
        val yyapisACEndpointId = BuildConfig.yyapisACEndpointId

        // デフォルト録音設定
        // サンプリングレート
        const val sampleRate = 16000

        // 音声のエンコード
        const val audioEncoding = "LINEAR16"

        // オーディオソース
        const val audioSource = MediaRecorder.AudioSource.MIC

        // チャンネル
        const val channel = AudioFormat.CHANNEL_IN_MONO

        // エンコード
        const val encoding = AudioFormat.ENCODING_PCM_16BIT

        // フレームレート
        const val flameRate = 10

        // 1フレームのバッファサイズ
        const val frameBufferSize = sampleRate / flameRate * 2

        // バッファサイズ
        val sizeInBytes = (frameBufferSize * 5).coerceAtLeast(
            AudioRecord.getMinBufferSize(
                sampleRate,
                channel,
                encoding
            )
        )
    }

}
