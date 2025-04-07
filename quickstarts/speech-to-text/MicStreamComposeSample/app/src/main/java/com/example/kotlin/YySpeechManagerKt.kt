package com.example.kotlin

import android.app.Activity
import android.util.Log
import com.example.kotlin.config.AppConfig
import com.google.protobuf.ByteString
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yysystem.StreamRequest
import yysystem.StreamResponse
import yysystem.StreamingConfig
import yysystem.YYSpeechGrpc
import java.util.concurrent.TimeUnit

class YySpeechManagerKt {
    private val data = ByteArray(AppConfig.FRAME_BUFFER_SIZE)
    private var recordingThread: Thread? = null
    private var call: StreamObserver<StreamRequest>? = null
    private var managedChannel: ManagedChannel? = null
    private val audioRecorder = AudioRecorder()
    private var streamingConfig = StreamingConfig.newBuilder().setEncoding(AppConfig.AUDIO_ENCODING)
        .setSampleRateHertz(AppConfig.SAMPLE_RATE).setLanguageCode(4).setModel(10)
        .setEnableInterimResults(true)
        .build()
    fun start(activity: Activity, yySpeechCallback: StreamObserver<StreamResponse>) {
        try {
            if (audioRecorder.isRecording) {
                return
            }
            managedChannel = if (AppConfig.YYAPIS_SSL) {
                ManagedChannelBuilder.forAddress(AppConfig.YYAPIS_ENDPOINT, AppConfig.YYAPIS_PORT)
                    .useTransportSecurity().enableRetry().build()
            } else {
                ManagedChannelBuilder.forAddress(AppConfig.YYAPIS_ENDPOINT, AppConfig.YYAPIS_PORT)
                    .usePlaintext().enableRetry().build()
            }
            val metadata = Metadata()
            metadata.put(
                Metadata.Key.of("yyapis-api-key", Metadata.ASCII_STRING_MARSHALLER),
                AppConfig.YYAPIS_API_KEY
            )
            val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
            val interceptedChannel = ClientInterceptors.intercept(managedChannel, interceptor)
            val yySpeech = YYSpeechGrpc.newStub(interceptedChannel)
            call = yySpeech.recognizeStream(yySpeechCallback)
            call?.onNext(StreamRequest.newBuilder().setStreamingConfig(streamingConfig).build())
            CoroutineScope(Dispatchers.IO).launch {
                delay(5000)
                val streamingConfig = StreamingConfig.newBuilder().setEnableInterimResults(false)
                    .addAutoDetectLanguageCodes(4).addAllTranslateTo(listOf()).build()
                Log.i(javaClass.simpleName, "send StreamingConfig request")
                call?.onNext(
                    StreamRequest.newBuilder().setStreamingConfig(streamingConfig).build()
                )
            }
            audioRecorder.start(activity)
            recordingThread = Thread({
                try {
                    while (audioRecorder.isRecording) {
                        audioRecorder.audioRecord?.read(data, 0, AppConfig.FRAME_BUFFER_SIZE)
                        call?.onNext(
                            StreamRequest.newBuilder().setAudiobytes(ByteString.copyFrom(data)).build()
                        )
                    }
                } catch (err: Exception) {
                    Log.e(javaClass.simpleName, err.toString())
                }
            }, "RecordingThread")
            recordingThread?.start()
        } catch (err: Exception) {
            Log.e(javaClass.simpleName, err.toString())
        }
    }
    fun stop() {
        audioRecorder.stop()
        call?.onCompleted()
        managedChannel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
        recordingThread?.interrupt()
        recordingThread = null
    }
}