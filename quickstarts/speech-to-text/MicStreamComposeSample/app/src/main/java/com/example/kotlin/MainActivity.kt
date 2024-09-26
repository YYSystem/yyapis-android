package com.example.kotlin

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.core.app.ActivityCompat
import com.example.kotlin.config.AppConfig
import io.grpc.stub.StreamObserver
import yysystem.StreamResponse

class MainActivity : AppCompatActivity() {

    // 音声認識管理クラスのインスタンス
    private lateinit var yySpeechManagerKt: YySpeechManagerKt

    // Activityのハンドラ
    private val handler = Handler(Looper.getMainLooper())

    // 自動スクロール
    private var isAutoScroll = true;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initYySpeechManager()
    }

    // 音声認識初期化処理
    private fun initYySpeechManager(){
        // 音声認識マネージャー初期化
        yySpeechManagerKt = YySpeechManagerKt()

        val editTextSpeechTextContent = findViewById<EditText>(R.id.editTextSpeechTextContent)

        // 音声認識コールバック
        val yySpeechCallback = object : StreamObserver<StreamResponse> {
            override fun onNext(response: StreamResponse) {
                val res = response.result ?: return
                if(res.transcript == "" || res.transcript == null){
                    return
                }
                Log.i(javaClass.simpleName, res.transcript)

                // ★：最終結果　☆：途中結果
                val transcript = if (res.isFinal) {
                    "★：${res.transcript}\r\n"
                } else {
                    "☆：${res.transcript}\r\n"
                }

                handler.post {
                    editTextSpeechTextContent.setText("${editTextSpeechTextContent.text}${transcript}")
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
                yySpeechManagerKt.streamingRestart()
            }
        }

        // ボタンの初期化
        initButton(yySpeechCallback)
    }

    // ボタンの初期化
    private fun initButton(callback: StreamObserver<StreamResponse>){
        var isButtonLock = false

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnClear = findViewById<Button>(R.id.btnClear)

        // 開始ボタンクリックイベント
        btnStart.setOnClickListener {
            if(isButtonLock){
                return@setOnClickListener
            }
            isButtonLock = true

            if(AppConfig.YYAPI_END_POINT.isEmpty() || AppConfig.YYAPI_PORT == 0 || AppConfig.YYAPI_API_KEY.isEmpty()){
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
                // 音声認識開始
                yySpeechManagerKt.startStreaming(this, callback)

                // ボタン有効化
                btnStart.isEnabled = false
                btnStop.isEnabled = true
            }
            catch (err:Exception){
                Log.e(javaClass.simpleName, err.toString())
            }
            isButtonLock = false
        }

        // 停止ボタンクリックイベント
        btnStop.setOnClickListener {
            if(isButtonLock){
                return@setOnClickListener
            }
            isButtonLock = true

            try {
                // 音声認識停止
                yySpeechManagerKt.stopStreaming()

                // ボタン有効化
                btnStart.isEnabled = true
                btnStop.isEnabled = false

            }
            catch (err:Exception){
                Log.e(javaClass.simpleName, err.toString())
            }

            isButtonLock = false
        }

        // クリアボタンクリックイベント
        btnClear.setOnClickListener {
            val editTextSpeechTextContent = findViewById<EditText>(R.id.editTextSpeechTextContent)
            editTextSpeechTextContent.setText("")
        }

        // ボタン有効化
        btnStart.isEnabled = true
        btnStop.isEnabled = false

    }
}