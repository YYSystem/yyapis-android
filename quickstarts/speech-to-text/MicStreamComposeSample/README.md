# Kotlin サンプル

## 実行環境

Android Studio

最低APIレベル 28

推奨エミュレータ：Pixel 3a XL

## 事前準備

**Android Studioをインストールしてください**

- Android Studioのインストール [<u>Android
Studio公式サイト</u>](https://developer.android.com/studio/install?hl=ja)

- 日本語化参考ページ
[<u>参考ページ</u>](https://cbtdev.net/android-studio-japanese/)

- 実行にはAndroid端末をUSBで接続、またはエミュレータが必要です。（エミュレータインストール参考ページ
[<u>参考ページ</u>](https://developer.android.com/studio/run/emulator?hl=ja)
）

**protocコマンドをインストールしてください**

音声認識はgRPC通信プロコトルで実行されます。protoファイルをJavaやKotlinファイルへコンパイルするために、protocコマンドのインストールが必要です。

\[windowsの場合\] Chocolateyでインストールする

管理者権限のPowershellで次のコマンドを実行してください。

```powershell
> choco install protoc
> protoc —version #インストールできたことを確認
libprotoc 24.3
```

\[Mac OSの場合\] Homebrewでインストールする

zshで次のコマンドを実行してください。

```bash
$ brew install probuf
$ protoc –version #インストールできたことを確認
libprotoc 3.21.12
```

ChocolateyやHomebrewを使わない場合は次のリンクを参考にしてコマンドをインストールしてください。

[<u>Protobuf Compiler
Installation</u>](https://github.com/protocolbuffers/protobuf#protobuf-compiler-installation)

## サンプルコードのダウンロード

サンプルコードのzipファイルを運営が配布します。受け取ったサンプルコードyyapis_samples_kotlin_speechtotext.zipを解凍してください。（パスが長いファイルがあるので、Cドライブ直下など階層が浅いディレクトリに解凍することをお勧めします。）次に、解凍されたyyapis_samples_kotlin_speechtotextからAndroidプロジェクトのkotlin-sampleをAndroidStudioのAndroidStudioProjectsに移動します。

yy-system-api-samples-develop-speech-to-text-kotlin-sample  
&emsp;/speech-to-text  
&emsp;&emsp;/kotlin-sample ←このファイルを移動する

## 実行

AndroidStudioProjectsに移動したkotlin-sample直下にkeystore.propertiesファイルを作成します。このファイルにyyapisのパラメータを設定します。

```
YYAPIS_API_KEY="YOUR_API_KEY"
YYAPIS_ENDPOINT="api-grpc-2.yysystem2021.com"
YYAPIS_PORT=443
YYAPIS_SSL=true
```

YOUR_API_KEYにはYYAPIs[<u>開発者コンソール</u>](https://api-web.yysystem2021.com)で生成したSpeech-to-TextのAPIキーの値を使用してください。値を入れる際はダブルクォーテーション（”）で囲む必要があるので注意してください。

次に、Android
Studioでkotlin-sampleプロジェクトを起動します。VCSのエラーが出る場合は、File
\> Settings \> Version Control Directory
Mappingsからエラーのでているディレクトリを削除してください。Protocol
Buffersのプラグインが提案されたときは、Android
Studioの指示に従ってプラグインを入れてください。

次の手順に従って、サンプルアプリを起動します。

- Gradleアイコン（Sync Project with Gradle
Files）をクリックして、Gradleファイルをプロジェクトに同期します。

- 再生アイコン（Run
‘app’）をクリックして、アプリケーションを実行します。

サンプルアプリが起動したら、画面下部のマイクアイコンを押して音声認識を開始してください。マイクアクセスの許可を求められた場合、許可した後、再度マイクアイコンを押してください。停止アイコンが押されるまで、発話している音声がリアルタイムに文字起こしされます。停止アイコンを押すと音声認識を停止します。
