# 我的浏览器 - Android 浏览器 App

## 功能
- 地址栏：支持网址输入和关键词搜索（自动跳转Google搜索）
- 前进/后退按钮
- 多标签页：顶部标签栏，可新增/关闭标签
- 加载进度条
- 深色主题界面

## 构建方法（推荐：Android Studio）

1. 下载解压此 ZIP 文件
2. 打开 Android Studio → File → Open，选择 `MyBrowser` 文件夹
3. 等待 Gradle 同步完成（首次需下载依赖，需要网络）
4. 连接 Android 手机（开启开发者模式 + USB调试）或启动模拟器
5. 点击 ▶ 运行按钮，或菜单 Build → Build Bundle(s)/APK(s) → Build APK(s)
6. APK 文件在：`app/build/outputs/apk/debug/app-debug.apk`

## 要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK (compileSdk 34, minSdk 21)
- 手机需要 Android 5.0 (Lollipop) 或更高版本

## 安装到手机
- 通过 USB：`adb install app/build/outputs/apk/debug/app-debug.apk`
- 或直接将 APK 文件传输到手机，在文件管理器中点击安装（需开启"允许未知来源"）
