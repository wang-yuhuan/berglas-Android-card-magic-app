param([string]$Label = 'current', [string]$Serial = 'emulator-5554', [string]$AdbPath)
$ErrorActionPreference = 'Stop'
if ($Label -notmatch '^[a-zA-Z0-9_-]+$') { throw 'Label must contain only letters, digits, underscores or hyphens.' }
$adb = $AdbPath
if (-not $adb) {
    foreach ($sdk in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, $(if ($env:LOCALAPPDATA) { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }))) {
        if ($sdk) {
            $candidate = Join-Path $sdk 'platform-tools/adb.exe'
            if (Test-Path -LiteralPath $candidate) { $adb = $candidate; break }
        }
    }
}
if (-not $adb) {
    $command = Get-Command adb -ErrorAction SilentlyContinue
    if ($command) { $adb = $command.Source }
}
if (-not $adb -or -not (Test-Path -LiteralPath $adb)) { throw 'ADB not found. Set ANDROID_HOME, add adb to PATH, or pass -AdbPath.' }
# Gesture coordinates below are for a 1080 x 2400 test device. Adjust for other sizes.
$outDir = Join-Path $PSScriptRoot '../docs/performance'
New-Item -ItemType Directory -Force $outDir | Out-Null
& $adb -s $Serial shell dumpsys gfxinfo com.atelier.cards reset | Out-Null
& $adb -s $Serial shell input swipe 450 1000 450 1650 500
Start-Sleep -Milliseconds 850
& $adb -s $Serial shell dumpsys gfxinfo com.atelier.cards framestats | Set-Content -LiteralPath (Join-Path $outDir "$Label-extraction.txt") -Encoding utf8
& $adb -s $Serial shell dumpsys gfxinfo com.atelier.cards reset | Out-Null
1..6 | ForEach-Object {
 & $adb -s $Serial shell input swipe 540 1150 780 1600 400
 & $adb -s $Serial shell input swipe 780 1600 540 1150 400
}
& $adb -s $Serial shell dumpsys gfxinfo com.atelier.cards framestats | Set-Content -LiteralPath (Join-Path $outDir "$Label-drag.txt") -Encoding utf8
Get-ChildItem $outDir -Filter "$Label-*.txt" | ForEach-Object {
 $_.Name
 Select-String -LiteralPath $_.FullName -Pattern 'Total frames rendered:|Janky frames:|50th percentile:|90th percentile:|95th percentile:|99th percentile:' | ForEach-Object {$_.Line}
}

