package com.example.sttmicstreamcomposesample

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sttmicstreamcomposesample.config.AppConfig
import com.example.sttmicstreamcomposesample.util.AudioRecorder
import com.google.protobuf.ByteString
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import yysystem.StreamRequest
import yysystem.StreamResponse
import yysystem.YYSpeechGrpcKt.YYSpeechCoroutineStub
import yysystem.streamRequest
import yysystem.streamingConfig

class MainViewModel: ViewModel() {
    private var managedChannel: ManagedChannel? = null
    private val data = ByteArray(AppConfig.frameBufferSize)
    private val _responseStream = MutableSharedFlow<StreamResponse>(replay = 0)
    val responseStream: Flow<StreamResponse> = _responseStream
    private val audioRecorder = AudioRecorder()
    private var recordingJob: Job? = null
    private var responseJob: Job? = null
    private val _isRecording = MutableStateFlow(false)
    val isRecording: Flow<Boolean> = _isRecording.asStateFlow()
    private val _isShuttingDown = MutableStateFlow(false)
    val isShuttingDown: Flow<Boolean> = _isShuttingDown.asStateFlow()
    private val _requestStream = MutableSharedFlow<StreamRequest>(replay = 0)
    fun start(activity: ComponentActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                managedChannel = when {
                    AppConfig.yyapisSsl -> ManagedChannelBuilder.forAddress(
                        AppConfig.yyapisEndpoint,
                        AppConfig.yyapisPort
                    ).useTransportSecurity().enableRetry().build()

                    else -> ManagedChannelBuilder.forAddress(
                        AppConfig.yyapisEndpoint,
                        AppConfig.yyapisPort
                    ).usePlaintext().enableRetry().build()
                }
                val metadata = Metadata()
                metadata.put(
                    Metadata.Key.of("yyapis-api-key", Metadata.ASCII_STRING_MARSHALLER),
                    AppConfig.yyapisApiKey
                )
                val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
                val interceptedChannel = ClientInterceptors.intercept(managedChannel, interceptor)
                val stub = YYSpeechCoroutineStub(interceptedChannel)
                val requestStream: Flow<StreamRequest> = _requestStream.map { request ->
                    request
                }
                val responseFlow = stub.recognizeStream(requestStream)
                responseJob = viewModelScope.launch(Dispatchers.IO) {
                    try {
                        responseFlow.collect {
                            Log.d(javaClass.simpleName, "Received response: ${it.toString()}")
                            _responseStream.emit(it)
                        }
                    } catch (e: CancellationException) {
                        Log.d(javaClass.simpleName, "response flow canceled: ${e.localizedMessage}")
                    } catch (e: Exception) {
                        Log.e(
                            javaClass.simpleName,
                            "Error in response stream: ${e.localizedMessage}"
                        )
                    } finally {
                        Log.d(javaClass.simpleName, "responseStream completed")
                    }
                }
                delay(100)
                _requestStream.emit(
                    streamRequest {
                        streamingConfig = streamingConfig {
                            sampleRateHertz = AppConfig.sampleRate
                            languageCode = 4
                            model = 10
                            encoding = AppConfig.audioEncoding
                            enableInterimResults = true
                        }
                    }
                )
                audioRecorder.start(activity)
                viewModelScope.launch(Dispatchers.Main) {
                    _isRecording.value = true
                }
                recordingJob = viewModelScope.launch(Dispatchers.IO) {
                    while (audioRecorder.isRecording) {
                        audioRecorder.audioRecord?.read(data, 0, AppConfig.frameBufferSize)
                        _requestStream.emit(
                            streamRequest {
                                audiobytes = ByteString.copyFrom(data)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "Error starting audio classification: ${e.localizedMessage}"
                )
                viewModelScope.launch(Dispatchers.Main) {
                    _isRecording.value = false
                }
            }
        }
    }
    fun stop() {
        Log.d(javaClass.simpleName, "stop メソッド開始")
        viewModelScope.launch(Dispatchers.Main) {
            Log.d(javaClass.simpleName, "stop メソッドのメインディスパッチャー開始")
            _isShuttingDown.value = true
            recordingJob?.cancelAndJoin()
            responseJob?.cancelAndJoin()
            audioRecorder.stop()
            shutdownChannel()
            delay(400)
            _isShuttingDown.value = false
            _isRecording.value = false
        }
    }
    private fun shutdownChannel() {
        try {
            val channel = managedChannel?.shutdown()
            if (channel?.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS) == false) {
                Log.w(javaClass.simpleName, "ManagedChannel shutdown timed out")
                channel.shutdownNow()
            } else {
                Log.d(javaClass.simpleName, "ManagedChannel shutdown完了")
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error shutting down ManagedChannel: ${e.localizedMessage}")
            managedChannel?.shutdownNow()
        }
        managedChannel = null
    }
    override fun onCleared() {
        super.onCleared()
        recordingJob?.cancel()
        responseJob?.cancel()
        audioRecorder.stop()
    }

}