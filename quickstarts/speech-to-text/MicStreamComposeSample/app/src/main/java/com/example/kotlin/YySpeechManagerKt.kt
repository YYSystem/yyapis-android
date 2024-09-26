package com.example.kotlin

import android.app.Activity
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlin.config.AppConfig
import com.google.gson.Gson
import com.google.protobuf.ByteString
import com.google.type.DateTime
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.grpc.stub.StreamObserver
import yysystem.StreamRequest
import yysystem.StreamResponse
import yysystem.StreamingConfig
import yysystem.YYSpeechGrpc
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.util.*
import kotlin.math.log10

class YySpeechManagerKt() {

    // バッファデータ
    private val data = ByteArray(AppConfig.FRAME_BUFFER_SIZE)

    // 録音データ取得スレッド
    private lateinit var recordingThread: Thread

    // APIへのリクエストストリームインスタンス
    private var call: StreamObserver<StreamRequest>? = null

    // コールバック定義
    private var callback : StreamObserver<StreamResponse>? = null // 音声認識結果のコールバック

    // ManagedChannelインスタンス(Grpc接続情報)
    private lateinit var managedChannel: ManagedChannel

    // 録音クラスインスタンス
    val audioRecorder = AudioRecorder()


    // 言語設定定義
    val remarkLanguageList = arrayOf("japanese","english","chinese","portugal")
    val speechApiLanguageList = arrayOf("jp","en","cn","pt")
    enum class SpeechLanguage {
        JAPANESE,
        ENGLISH,
        CHINESE,
        PORTUGAL
    }

    // APIの設定(デフォルト値)
    private val defaultStreamingConfig = SendStreamingConfig(
        AppConfig.AUDIO_ENCODING,
        AppConfig.SAMPLE_RATE,
        SpeechLanguage.JAPANESE.ordinal,
        1,  // 0: 高精度OFF 1:高精度ON
        enable_word = false,
        enable_interim_results = true,
        translateTo = arrayOf()
    )

    // APIの設定
    private var streamingConfig = defaultStreamingConfig

    // 音声認識開始
    fun startStreaming(activity: Activity, yySpeechCallback: StreamObserver<StreamResponse>) {
        try {

            // 録音中なら停止させるだけ
            if (audioRecorder.isRecording()) {
                stopStreaming()
                return
            }

            // 接続インスタンス作成
            callback = yySpeechCallback
            createConnectGrpc()

            // 録音開始
            audioRecorder.recordStart(activity)
            audioRecorder.audioRecord!!.read(data, 0, AppConfig.FRAME_BUFFER_SIZE)

            // 録音データ取得スレッド開始
            recordingThread = Thread({ readRecordData() }, "RecordingThread")
            recordingThread.start()

        } catch (err: Exception) {
            Log.e(javaClass.simpleName, err.toString())
        }
    }

    // 音声認識停止
    fun stopStreaming() {
        audioRecorder.recordStop()
    }

    // 再接続する処理
    fun streamingRestart(){
        // 録音中のみ実行
        if(!audioRecorder.isRecording()){
            return
        }
        release()
        // 接続インスタンス作成
        createConnectGrpc()
    }

    // 録音データ取得スレッドの処理
    private fun readRecordData() {
        try {

            while (audioRecorder.isRecording()) {
                // 音声データ読み込み
                audioRecorder.audioRecord!!.read(data, 0, AppConfig.FRAME_BUFFER_SIZE)

                // 音声データ送信
                call!!.onNext(StreamRequest.newBuilder().setAudiobytes(ByteString.copyFrom(data)).build())
            }

        } catch (err: Exception) {
            Log.e(javaClass.simpleName, err.toString())
        }

        // 録音インスタンスリリース
        audioRecorder.release()

        // インスタンスリリース
        release()
    }

    // 各種インスタンスの解放
    private fun release() {
        // grpc接続切断
        if (call != null) {
            call = null
        }
        managedChannel.shutdown()
    }

    // メタデータ取得
    private fun getMetadata(): Metadata {
        val res = Metadata()
        try {
            // StreamingConfigデータ作成
            val strSC = Gson().toJson(streamingConfig)

            // APIキーをセット
            res.put(
                Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER),
                AppConfig.YYAPI_API_KEY
            )

            // StreamingConfigをセット
            res.put(Metadata.Key.of("x-streaming-config", Metadata.ASCII_STRING_MARSHALLER), strSC)
        } catch (err: Exception) {
            Log.e(javaClass.simpleName, err.toString())
        }
        return res
    }

    // YYAPI接続情報作成
    private fun createConnectGrpc() {
        try {
            // メタデータ(ヘッダデータ)作成
            val metaData = getMetadata()
            // 接続情報作成
            managedChannel = if(AppConfig.YYAPI_END_POINT_SSL_CONNECTION){
                ManagedChannelBuilder.forAddress(AppConfig.YYAPI_END_POINT, AppConfig.YYAPI_PORT)
                    .useTransportSecurity()
                    .build()
            } else {
                ManagedChannelBuilder.forAddress(AppConfig.YYAPI_END_POINT, AppConfig.YYAPI_PORT)
                    .usePlaintext()
                    .build()
            }
            val yySpeech =
                MetadataUtils.attachHeaders(YYSpeechGrpc.newStub(managedChannel), metaData)
            // YYAPIコールバック設定
            call = yySpeech.recognizeStream(callback)
        } catch (err: Exception) {
            Log.e(javaClass.simpleName, err.toString())
        }
    }


    // 言語設定の変更
    fun setSpeechLanguage(lang: SpeechLanguage){
        try {
            streamingConfig.language_code = lang.ordinal

            // 設定送信
            if(call != null){
                val sc = StreamingConfig.newBuilder()
                    .setEncoding(streamingConfig.encoding)
                    .setSampleRateHertz(streamingConfig.sample_rate_hertz)
                    .setLanguageCode(streamingConfig.language_code)
                    .setModel(streamingConfig.model)
                    .setEnableWord(streamingConfig.enable_word)
                    .setEnableInterimResults(streamingConfig.enable_interim_results)
                    .build()
                val req = StreamRequest.newBuilder().setStreamingConfig(sc).build()
                call!!.onNext(req)

            }

        } catch (err: Exception){
            Log.e(javaClass.simpleName, err.toString())
        }
    }
}

data class SendStreamingConfig(
    val encoding : String,
    val sample_rate_hertz : Int,
    var language_code : Int,
    val model : Int,
    val enable_word : Boolean,
    val enable_interim_results : Boolean,
    val translateTo : Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendStreamingConfig
        if (encoding != other.encoding) return false
        if (sample_rate_hertz != other.sample_rate_hertz) return false
        if (language_code != other.language_code) return false
        if (model != other.model) return false
        if (enable_word != other.enable_word) return false
        if (enable_interim_results != other.enable_interim_results) return false
        if (!translateTo.contentEquals(other.translateTo)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = encoding.hashCode()
        result = 31 * result + sample_rate_hertz
        result = 31 * result + language_code
        result = 31 * result + model
        result = 31 * result + enable_word.hashCode()
        result = 31 * result + enable_interim_results.hashCode()
        result = 31 * result + translateTo.contentHashCode()
        return result
    }
}