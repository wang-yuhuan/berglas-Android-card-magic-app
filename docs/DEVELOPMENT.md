# 开发说明 / Development

## 环境 / Requirements

JDK 17；Android Studio（支持 AGP 8.9.2）；Android SDK Platform 35；Build-Tools 35.0.0。
JDK 17; Android Studio compatible with AGP 8.9.2; Android SDK Platform 35; Build-Tools 35.0.0.

项目固定使用 Gradle 8.11.1（自带 Wrapper）和 Kotlin 2.1.20。
The project pins Gradle 8.11.1 through its included wrapper and Kotlin 2.1.20.

## Android Studio

打开仓库根目录，设置 Gradle JDK 为 17，安装上述 SDK 并同步，选择 app 和模拟器或开启 USB 调试的手机，点击运行。让 Studio 生成 local.properties，不要提交本机 SDK 路径。

Open the repository root, select JDK 17 as the Gradle JDK, install the SDK packages, and sync. Select app and an emulator or a phone with USB debugging enabled, then Run. Let Studio generate local.properties; do not commit local SDK paths.

## 命令行 / Command line

设置 JAVA_HOME 指向 JDK 17，通过 ANDROID_HOME 或 local.properties 配置 SDK。
Set JAVA_HOME to JDK 17 and configure the SDK with ANDROID_HOME or local.properties.

Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# 已连接设备 / With a connected device
.\gradlew.bat :app:connectedDebugAndroidTest
```

macOS / Linux:

```sh
chmod +x gradlew
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Windows 也可运行 tools/build.ps1，添加 -DeviceTests 可运行设备测试。
Windows users can also run tools/build.ps1, adding -DeviceTests for device tests.

APK 导出到 dist/AtelierCards-debug.apk。Windows 非 ASCII 路径下，中间产物存放在系统临时目录。
The APK is exported to dist/AtelierCards-debug.apk. On Windows, non-ASCII project paths use the system temporary directory for intermediate outputs.

## 结构 / Layout

| 路径 / Path | 用途 / Purpose |
| --- | --- |
| app/src/main/java/com/atelier/cards/domain | 领域状态与规则 / Domain state and rules |
| app/src/main/java/com/atelier/cards/data | 偏好与状态恢复 / Preferences and restoration |
| app/src/main/java/com/atelier/cards/ui | 界面、动画与手势 / UI, animations, gestures |
| app/src/main/assets/cards | 52 张牌面 / 52 card faces |
| app/src/main/res/drawable-nodpi | 牌背和背景 / Card backs and background |
| app/src/test、app/src/androidTest | 单元和设备测试 / Unit and device tests |
| artwork、tools | 原始美术及开发工具 / Source artwork and tooling |

## 性能采样 / Performance sampling

tools/profile.ps1 自动查找 SDK 或 PATH 中的 ADB，支持 -AdbPath 与 -Serial。它会操作设备，请在应用初始界面、专用测试设备上运行。坐标针对 1080 × 2400，其他尺寸需调整。报告保存在本地 docs/performance；模拟器结果不能代替真机验收。

tools/profile.ps1 discovers ADB from SDK locations or PATH and accepts -AdbPath and -Serial. It operates the device: use a dedicated test device with the app in its initial state. Coordinates assume 1080 × 2400 and need adjustment for other sizes. Reports stay in local docs/performance; emulator results do not replace real-device checks.

## 发布 / Publishing

提交源码，将 APK 放在 Releases。正式分发使用自己的签名密钥，密钥与密码不要提交。发布前检查暂存文件列表；不要直接上传整个本地文件夹或旧版源码 ZIP。内部笔记和历史报告已加入忽略规则。

Commit source and attach APKs to Releases. Use your own signing key for production, keeping keys and passwords private. Review staged files before publishing. Do not upload the entire working folder or old source ZIPs. Internal notes and historical reports are ignored.
