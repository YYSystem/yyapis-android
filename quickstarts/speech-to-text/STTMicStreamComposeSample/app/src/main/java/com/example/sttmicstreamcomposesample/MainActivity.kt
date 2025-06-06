package com.example.sttmicstreamcomposesample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sttmicstreamcomposesample.ui.theme.STTMicStreamComposeSampleTheme
import yysystem.StreamResponse
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.grpc.stub.StreamObserver

class MainActivity : ComponentActivity() {
    lateinit var yySpeechManager: YySpeechManager
    private lateinit var permissionManager: PermissionManager
    private var showPermissionDeniedDialog by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initYySpeechManager()
        permissionManager = PermissionManager(this)
        setContent {
            STTMicStreamComposeSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onStart = { startRecording(it) }
                    )
                    if (showPermissionDeniedDialog) {
                        PermissionDeniedDialog(
                            onDismiss = { showPermissionDeniedDialog = false }
                        )
                    }
                }
            }
        }
    }
    private fun initYySpeechManager() {
        yySpeechManager = YySpeechManager()
    }
    private fun startRecording(onPermissionGranted: () -> Unit) {
        Log.i(javaClass.simpleName, "Starting recording")
        permissionManager.requestMicPermission(
            onGranted = {
                Log.i(javaClass.simpleName, "Microphone permission granted")
                onPermissionGranted()
            },
            onDenied = {
                Log.w(javaClass.simpleName, "Microphone permission denied")
                runOnUiThread {
                    Log.w(javaClass.simpleName, "Showing permission denied dialog")
                    showPermissionDeniedDialog = true
                }
            }
        )
    }
}

@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "権限エラー") },
        text = { Text(text = "このアプリはマイクの使用を必要です。設定からマイクの権限を許可してください。") },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                activity?.finish()
            }) {
                Text(text = "終了")
            }
        }
    )
}

@Composable
fun MainScreen(onStart: (() -> Unit) -> Unit) {
    var transcriptText by remember { mutableStateOf("") }
    var isButtonLocked by remember { mutableStateOf(false) }
    var isStartEnabled by remember { mutableStateOf(true) }
    var isStopEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? MainActivity
    val yySpeechCallback = object : StreamObserver<StreamResponse> {
        override fun onNext(chunk: StreamResponse) {
            // Handle the response from the server
            val res = chunk.result
            if (res.transcript.isNullOrEmpty()) {
                Log.i(javaClass.simpleName, "Received empty transcript")
                return
            }
            Log.i(javaClass.simpleName, "Received transcript: ${res.transcript}")
            val transcript = if (res.isFinal) {
                res.transcript
            } else {
                res.transcript + "..."
            }
            activity?.let { act ->
                act.runOnUiThread {
                    // Update UI with the received transcript
                    transcriptText = transcript
                    Log.i(javaClass.simpleName, "Updated transcript text: $transcriptText")
                }
            }
        }

        override fun onError(t: Throwable) {
            Log.e(javaClass.simpleName, "Error in speech recognition: ${t.message}")
        }

        override fun onCompleted() {
            Log.i(javaClass.simpleName, "Speech recognition completed")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "音声認識サンプル",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = transcriptText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        Log.i(javaClass.simpleName, "Start button clicked")
                        if (isButtonLocked) return@Button
                        isButtonLocked = true
                        onStart {
                            activity?.let { act ->
                                try {
                                    (act as? MainActivity)?.yySpeechManager?.start(
                                        act,
                                        yySpeechCallback
                                    )
                                    isStartEnabled = false
                                    isStopEnabled = true
                                } catch (e: Exception) {
                                    Log.e(
                                        javaClass.simpleName,
                                        "Error starting speech recognition: ${e.message}"
                                    )
                                } finally {
                                    isButtonLocked = false
                                }

                            }
                        }
                    },
                    enabled = isStartEnabled,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "開始")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (isButtonLocked) return@Button
                        isButtonLocked = true
                        activity?.let { act ->
                            try {
                                (act as? MainActivity)?.yySpeechManager?.stop()
                                isStartEnabled = true
                                isStopEnabled = false
                            } catch (e: Exception) {
                                Log.e(
                                    javaClass.simpleName,
                                    "Error stopping speech recognition: ${e.message}"
                                )
                            } finally {
                                isButtonLocked = false
                            }
                        }
                    },
                    enabled = isStopEnabled,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "停止")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    STTMicStreamComposeSampleTheme {
        MainScreen(
            onStart = { callback ->
                callback()
            }
        )
    }
}