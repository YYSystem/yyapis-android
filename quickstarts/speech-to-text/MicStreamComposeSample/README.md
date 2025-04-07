# Kotlin サンプル

## 実行環境

Android Studio

最低 API レベル 28

## 事前準備

- Android Studio

  Android Studio のインストール [<u>Android
  Studio 公式サイト</u>](https://developer.android.com/studio/install?hl=ja)

  日本語化参考ページ
  [<u>参考ページ</u>](https://cbtdev.net/android-studio-japanese/)

  実行には Android 端末を USB で接続、またはエミュレータが必要です。（エミュレータインストール参考ページ
  [<u>参考ページ</u>](https://developer.android.com/studio/run/emulator?hl=ja)
  ）

- YYAPIs 音声認識サービスの API キーと proto ファイル(`yysystem.proto`)

  API キーと proto ファイルを取得するには、[YYAPIs 開発者コンソール](https://api-web.yysystem2021.com) のアカウントが必要です。
  開発者コンソールの詳しい使い方については、こちらの[ドキュメント](https://github.com/YYSystem/yyapis-docs/wiki/DeveloperConsole)を参考にしてください。

- プロトコルバッファー protobuf

  音声認識は gRPC 通信プロコトルで実行されます。proto ファイルを Java や Kotlin ファイルへコンパイルするために、protoc コマンドのインストールが必要です。

  \[windows の場合\] Chocolatey でインストールする

  管理者権限の Powershell で次のコマンドを実行してください。

  ```powershell
  > choco install protoc
  > protoc —version #インストールできたことを確認
  ```

  \[Mac OS の場合\] Homebrew でインストールする

  zsh で次のコマンドを実行してください。

  ```bash
  $ brew install probuf
  $ protoc –-version #インストールできたことを確認
  ```

  Chocolatey や Homebrew を使わない場合は次のリンクを参考にしてコマンドをインストールしてください。

  [<u>Protobuf Compiler
  Installation</u>](https://github.com/protocolbuffers/protobuf#protobuf-compiler-installation)

## セットアップ

次のリンクから yyapis-android の github プロジェクトを clone してください。 （ダウンロード後対象のアプリを C ドライブ直下など階層が浅いディレクトリに移動することをお勧めします。）

[<u>yyapis-android</u>](https://github.com/YYSystem/yyapis-android.git)

```bash
yyapis-android/quickstarts/speech-to-text/MicStreamComposeSample # ← このディレクトリを移動する
```

AndroidStudioProjects に移動した MicStreamComposeSample 直下に keystore.properties ファイルを作成します。このファイルに yyapis のパラメータを設定します。

```
YYAPIS_API_KEY=YOUR_API_KEY
YYAPIS_ENDPOINT=api-grpc-2.yysystem2021.com
YYAPIS_PORT=443
YYAPIS_SSL=true
```

YOUR_API_KEY には YYAPIs[<u>開発者コンソール</u>](https://api-web.yysystem2021.com)で生成した Speech-to-Text の API キーの値を使用してください。

次に proto ファイルを `MicStreamComposeSample/app/src/main/proto` に移動します。

```bash
MicStreamComposeSample/app/src/main/proto/yysystem.proto
```

Android Studio で MicStreamComposeSample プロジェクトを起動します。VCS のエラーが出る場合は、File \> Settings \> Version Control Directory Mappings からエラーのでているディレクトリを削除してください。Protocol Buffers のプラグインが提案されたときは、Android Studio の指示に従ってプラグインを入れてください。
手動でプラグインを入れる場合は、Settings -> Plugins の検索欄に「Protocol Buffers」を入力し、Installをクリックしてください。

## 実行

次の手順に従って、Android Studio からサンプルアプリを起動します。

- Gradle アイコン（Sync Project with Gradle
  Files）をクリックして、Gradle ファイルをプロジェクトに同期します。

- 再生アイコン（Run
  ‘app’）をクリックして、アプリケーションを実行します。

サンプルアプリが起動したら、画面下部の開始ボタンをタップして音声認識を開始してください。マイクアクセスの許可を求められた場合、許可した後、再度開始ボタンをタップしてください。停止ボタンがタップされるまで、発話している音声がリアルタイムに文字起こしされます。停止ボタンをタップすると音声認識を停止します。

