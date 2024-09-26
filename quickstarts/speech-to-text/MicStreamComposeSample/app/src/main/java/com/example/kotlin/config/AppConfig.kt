package com.example.kotlin.config

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class AppConfig {

     companion object {
         // 接続先設定
         const val YYAPI_END_POINT = ""
         const val YYAPI_PORT = 0
         const val YYAPI_API_KEY = ""
         const val YYAPI_END_POINT_SSL_CONNECTION = true

         // デフォルト録音設定
         // サンプリングレート
         const val SAMPLE_RATE = 16000

         // 音声のエンコード
         const val AUDIO_ENCODING = "LINEAR16"

         // オーディオソース
         const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

         // チャンネル
         const val CHANNEL = AudioFormat.CHANNEL_IN_MONO

         // エンコード
         const val ENCODING = AudioFormat.ENCODING_PCM_16BIT

         // フレームレート
         const val FLAME_RATE = 10

         // 1フレームのバッファサイズ
         const val FRAME_BUFFER_SIZE = SAMPLE_RATE / FLAME_RATE * 2

         // バッファサイズ
         val SIZE_IN_BYTES = (FRAME_BUFFER_SIZE * 5).coerceAtLeast(
             AudioRecord.getMinBufferSize(
                 SAMPLE_RATE,
                 CHANNEL,
                 ENCODING
             )
         )
     }

}