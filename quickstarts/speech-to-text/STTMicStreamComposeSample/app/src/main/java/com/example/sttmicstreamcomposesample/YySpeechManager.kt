package com.example.sttmicstreamcomposesample

import android.app.Activity
import android.util.Log
import com.example.sttmicstreamcomposesample.config.AppConfig
import com.google.protobuf.ByteString
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.grpc.stub.StreamObserver
import yysystem.StreamRequest
import yysystem.StreamResponse
import yysystem.StreamingConfig
import yysystem.YYSpeechGrpc
import java.util.concurrent.TimeUnit

class YySpeechManager {
    private val data = ByteArray(AppConfig.frameBufferSize)
    private var recordingThread: Thread? = null
    private var call: StreamObserver<StreamRequest>? = null
    private var managedChannel: ManagedChannel? = null
    private val audioRecorder = AudioRecorder()
    private var streamingConfig = StreamingConfig.newBuilder().setEncoding(AppConfig.audioEncoding)
        .setSampleRateHertz(AppConfig.sampleRate).setLanguageCode(4).setModel(10)
        .setEnableInterimResults(true)
        .build()
    fun start(activity: Activity, yySpeechCallback: StreamObserver<StreamResponse>) {
        try {
            if (audioRecorder.isRecording) {
                return
            }
            managedChannel = if (AppConfig.yyapisSsl) {
                ManagedChannelBuilder.forAddress(AppConfig.yyapisEndpoint, AppConfig.yyapisPort)
                    .useTransportSecurity().enableRetry().build()
            } else {
                ManagedChannelBuilder.forAddress(AppConfig.yyapisEndpoint, AppConfig.yyapisPort)
                    .usePlaintext().enableRetry().build()
            }
            val metadata = Metadata()
            metadata.put(
                Metadata.Key.of("yyapis-api-key", Metadata.ASCII_STRING_MARSHALLER),
                AppConfig.yyapisApiKey
            )
            val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
            val interceptedChannel = ClientInterceptors.intercept(managedChannel, interceptor)
            val yySpeech = YYSpeechGrpc.newStub(interceptedChannel)
            call = yySpeech.recognizeStream(yySpeechCallback)
            call?.onNext(StreamRequest.newBuilder().setStreamingConfig(streamingConfig).build())
            audioRecorder.start(activity)
            recordingThread = Thread({
                try {
                    while (audioRecorder.isRecording) {
                        audioRecorder.audioRecord?.read(data, 0, AppConfig.frameBufferSize)
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
