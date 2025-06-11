package com.example.acstreamcomposesample.utils

import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.acstreamcomposesample.MainActivity

class PermissionManager(private val activity: MainActivity) {
    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Log.i(
                javaClass.simpleName,
                "Microphone permission granted: $isGranted"
            )
            when {
                isGranted -> grantedCallback?.invoke()
                else -> deniedCallback?.invoke()
            }
            grantedCallback = null
            deniedCallback = null
        }
    private var grantedCallback: (() -> Unit)? = null
    private var deniedCallback: (() -> Unit)? = null
    fun requestMicPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        Log.i(
            javaClass.simpleName,
            "Requesting microphone permission"
        )
        when {
            hasMicPermission() -> {
                Log.i(
                    javaClass.simpleName,
                    "Microphone permission already granted"
                )
                onGranted()
            }
            else -> {
                grantedCallback = onGranted
                deniedCallback = onDenied
                requestPermissionLauncher.launch(
                    android.Manifest.permission.RECORD_AUDIO
                )
            }

        }
    }
    private fun hasMicPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
