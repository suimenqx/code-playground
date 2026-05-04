# 生成 APK 的可行方式

## GitHub Actions 云端构建

本仓库已经包含 `.github/workflows/build-slice-rush-apk.yml`。

1. 打开仓库的 Actions 页面。
2. 选择 `Build Slice Rush Android APK`。
3. 点击 `Run workflow`，或等待 push 后自动运行。
4. 构建完成后，在 workflow 页面底部下载 `SliceRush-debug-apk` artifact。

## Android Studio 本地构建

1. 用 Android Studio 打开 `slice-rush-android/`。
2. 等待 Gradle Sync 完成。
3. 点击 `Build > Build Bundle(s) / APK(s) > Build APK(s)`。
4. APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`。

## 命令行本地构建

需要本机安装 JDK 17、Android SDK、Gradle 8.x。

```bash
cd slice-rush-android
./scripts/build-local.sh
```

## 版权说明

本项目是原创“切水果”游戏，不包含《水果忍者》原版代码、图片、音效或商标素材。
