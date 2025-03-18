package com.example.kotlin

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.kotlin.config.AppConfig
import com.example.kotlin.databinding.ActivityMainBinding
import io.grpc.stub.StreamObserver
import yysystem.StreamResponse

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var yySpeechManagerKt: YySpeechManagerKt
    private var isAutoScroll = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initYySpeechManager()
    }
    private fun initYySpeechManager(){
        yySpeechManagerKt = YySpeechManagerKt()
        val editTextSpeechTextContent = binding.editTextSpeechTextContent
        val yySpeechCallback = object : StreamObserver<StreamResponse> {
            override fun onNext(response: StreamResponse) {
                val res = response.result ?: return
                if(res.transcript == "" || res.transcript == null){
                    return
                }
                Log.i(javaClass.simpleName, res.transcript)
                val transcript = if (res.isFinal) {
                    "★：${res.transcript}\r\n"
                } else {
                    "☆：${res.transcript}\r\n"
                }
                runOnUiThread {
                    editTextSpeechTextContent.append(transcript)
                    if(isAutoScroll){
                        editTextSpeechTextContent.requestFocus()
                        editTextSpeechTextContent.setSelection(editTextSpeechTextContent.text.length)
                    }
                }

            }
            override fun onError(t: Throwable) {
                Log.i(javaClass.simpleName, "onError")
                Log.e(javaClass.simpleName, t.toString())
            }
            override fun onCompleted() {
                Log.i(javaClass.simpleName, "onCompleted")
            }
        }
        initButton(yySpeechCallback)
    }
    private fun initButton(callback: StreamObserver<StreamResponse>){
        var isButtonLock = false
        val btnStart = binding.btnStart
        val btnStop = binding.btnStop
        val btnClear = binding.btnClear
        btnStart.setOnClickListener {
            if(isButtonLock){
                return@setOnClickListener
            }
            isButtonLock = true
            if(AppConfig.YYAPIS_ENDPOINT.isEmpty() || AppConfig.YYAPIS_PORT == 0 || AppConfig.YYAPIS_API_KEY.isEmpty()){
                AlertDialog.Builder(this)
                    .setTitle("エラー")
                    .setMessage("AppConfigの接続先の設定が入力されていません。")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                return@setOnClickListener
            }
            try {
                yySpeechManagerKt.start(this, callback)
                btnStart.isEnabled = false
                btnStop.isEnabled = true
            }
            catch (err:Exception){
                Log.e(javaClass.simpleName, err.toString())
            } finally {
                isButtonLock = false
            }
        }
        btnStop.setOnClickListener {
            if(isButtonLock){
                return@setOnClickListener
            }
            isButtonLock = true
            try {
                yySpeechManagerKt.stop()
                btnStart.isEnabled = true
                btnStop.isEnabled = false

            }
            catch (err:Exception){
                Log.e(javaClass.simpleName, err.toString())
            } finally {
                isButtonLock = false
            }
        }
        btnClear.setOnClickListener {
            val editTextSpeechTextContent = binding.editTextSpeechTextContent
            editTextSpeechTextContent.setText("")
        }
        btnStart.isEnabled = true
        btnStop.isEnabled = false
    }
}