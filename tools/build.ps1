param([switch]$DeviceTests)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$portableJdk = Join-Path $projectRoot '.tools/jdk/jdk-17.0.20.1+1'
$originalJava = $env:JAVA_HOME
$originalGradle = $env:GRADLE_USER_HOME
try {
    if (Test-Path -LiteralPath (Join-Path $portableJdk 'bin/java.exe')) {
        $env:JAVA_HOME = $portableJdk
    } elseif (-not $env:JAVA_HOME) {
        throw 'Please install JDK 17 and set JAVA_HOME first. See README.md.'
    }
    # Keep Gradle worker classpaths ASCII on Windows, even for a Chinese project path.
    $env:GRADLE_USER_HOME = Join-Path $env:TEMP 'atelier-cards-gradle'
    Push-Location $projectRoot
    try {
        $tasks = @(':app:assembleDebug', ':app:testDebugUnitTest', ':app:lintDebug')
        if ($DeviceTests) { $tasks += ':app:connectedDebugAndroidTest' }
        & ./gradlew.bat @tasks '-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8' '--console=plain'
        if ($LASTEXITCODE -ne 0) { throw "Gradle exited with code $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $originalJava
    $env:GRADLE_USER_HOME = $originalGradle
}
