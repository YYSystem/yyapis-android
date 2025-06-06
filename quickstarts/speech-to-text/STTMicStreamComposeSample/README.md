# STTMicStreamComposeSample

## 概要

このアプリは Android で YYAPIs の音声認識サービスを使用してリアルタイムの音声文字起こしを実行します。

## 実行環境

- Android Studio Meerkat | 2024.3.1
- 最低 API レベル 28
- Kotlin 2.0.21

## 事前準備

- [Android Studio](https://developer.android.com/studio?hl=ja)
  Android 端末実機で実行する必要があります [参考ページ](https://developer.android.com/studio/run/device?hl=ja)
- YYAPIs 音声認識サービスの API キーと proto ファイル(`yysystem.proto`)
  API キーと proto ファイルは、[YYAPIs 開発者コンソール](https://api-web.yysystem2021.com) のアカウントが必要です。開発者コンソールの詳しい使い方については、[こちらのドキュメント](https://github.com/YYSystem/yyapis-docs/wiki/DeveloperConsole)を参考にしてください。

## セットアップ

次のリンクから yyapis-android の github プロジェクトを clone してください。 （ダウンロード後対象のアプリを C ドライブ直下など階層が浅いディレクトリに移動することをお勧めします。）

[yyapis-android](https://github.com/YYSystem/yyapis-android.git)

```bash
yyapis-android/quickstarts/speech-to-text/STTMicStreamComposeSample # ← このディレクトリを移動する
```

移動した `STTMicStreamComposeSample` 直下に local.properties ファイルを作成します。このファイルに yyapis のパラメータを設定します。

```
YYAPIS_API_KEY=YOUR_API_KEY
YYAPIS_ENDPOINT=api-grpc-2.yysystem2021.com
YYAPIS_PORT=443
YYAPIS_SSL=true
```

YOUR_API_KEY には YYAPIs[開発者コンソール](https://api-web.yysystem2021.com)で生成した Speech-to-Text の API キーの値を使用してください。

次に proto ファイルを `STTMicStreamComposeSample/app/src/main/proto` に移動します。

```bash
MicStreamComposeSample/app/src/main/proto/yysystem.proto
```

Android Studio で MicStreamComposeSample プロジェクトを起動します。

VCS のエラーが出る場合は、File \> Settings \> Version Control Directory Mappings からエラーのでているディレクトリを削除してください。

Protocol Buffers のプラグインが提案されたときは、Android Studio の指示に従ってプラグインを入れてください。

手動でプラグインを入れる場合は、Settings -> Plugins の検索欄に「Protocol Buffers」を入力し、Installをクリックしてください。

## 実行

次の手順に従って、Android Studio からサンプルアプリを起動します。

- Gradle アイコン (`Sync Project with Gradle Files`) をクリックして、Gradle の同期を行います。
- 実行デバイスを選択します。実機のAndroid 端末を選択してください。
- 再生アイコン (`Run 'app'`) をクリックして、アプリを実行します。

サンプルアプリが起動したら、画面内の開始ボタンをタップして音声認識を開始してください。

マイクアクセスの許可が求められた場合は、許可を与えてください。

音声認識が開始されると、認識されたテキストが画面に表示されます。

停止ボタンをタップすると音声認識が停止します。


## その他参考

[gRPC Kotlin を Android アプリに導入する構成ガイド](https://qiita.com/natsuki3624/items/06c5f0c0ce08177d6f0e)