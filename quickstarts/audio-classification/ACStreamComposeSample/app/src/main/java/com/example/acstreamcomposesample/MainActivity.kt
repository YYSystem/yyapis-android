package com.example.acstreamcomposesample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.acstreamcomposesample.ui.theme.ACStreamComposeSampleTheme
import com.example.acstreamcomposesample.utils.PermissionManager
import kotlinx.coroutines.launch
import yysystem.audioclassification.ClassifyStreamResponse
import yysystem.audioclassification.Result

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager
    private var showPermissionDeniedDialog by mutableStateOf(false)
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        permissionManager = PermissionManager(this)
        setContent {
            ACStreamComposeSampleTheme {
                MainScreen(viewModel = mainViewModel, onStart = { start() })
                if (showPermissionDeniedDialog) {
                    PermissionDeniedDialog(
                        onDismiss = { showPermissionDeniedDialog = false }
                    )
                }
            }
        }
    }
    private fun start() {
        permissionManager.requestMicPermission(
            onGranted = {
                Log.i(javaClass.simpleName, "マイクの権限が許可されました")
                lifecycleScope.launch {
                    try {
                        mainViewModel.start(this@MainActivity)
                    } catch (e: Exception) {
                        Log.e(javaClass.simpleName, "Error starting audio classification: ${e.localizedMessage}")
                    }
                }
            },
            onDenied = {
                Log.w(javaClass.simpleName, "マイクの権限が拒否されました")
                showPermissionDeniedDialog = true
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
fun MainScreen(viewModel: MainViewModel, onStart: () -> Unit) {
    val isRecording by viewModel.isRecording.collectAsState(false)
    val isShuttingDown by viewModel.isShuttingDown.collectAsState(false)
    val responseState = remember { mutableStateOf<ClassifyStreamResponse?>(null) }
    LaunchedEffect (Unit) {
        viewModel.responseStream.collect { response ->
            responseState.value = response
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                responseState.value?.let { response ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "custom_result: Top ${response.customResultsList.size}",
                            modifier = Modifier.padding(16.dp)
                        )
                        response.customResultsList.forEach { result ->
                            ResultItem(
                                result= result
                            )
                        }
                        Text(
                            text = "default_results: Top ${response.customResultsList.size}",
                            modifier = Modifier.padding(16.dp)
                        )
                        response.defaultResultsList.forEach { result ->
                            ResultItem(
                                result= result
                            )
                        }
                    }
                } ?: run {
                    when {
                        isRecording -> Text(text = "認識中...")
                        else -> Text(text = "音声認識を開始してください")
                    }
                }
                Spacer(modifier = Modifier.padding(16.dp))
                LoadingButton(onClick = {
                    when {
                        isRecording -> viewModel.stop()
                        else -> {
                            onStart()
                        }
                    }
                }, loading = isShuttingDown) {
                    Text(
                        text = when {
                            isRecording -> "停止"
                            else -> "開始"
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun LoadingButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    loading: Boolean,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading
    ) {
        Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    content()
                }
                else -> {
                    content()
                }
            }
        }
    }
}

@Composable
fun ResultItem(result: Result, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "className: ${result.className}")
        Text(text = "classId: ${result.classId}")
        Text(text = "confidence: ${result.confidence}")
        LinearProgressIndicator(
            progress = {result.confidence},
            modifier = Modifier.fillMaxWidth()
        )

    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ACStreamComposeSampleTheme {
        MainScreen(viewModel = MainViewModel(), onStart = { /* Do nothing in preview */ })
    }
}