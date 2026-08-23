$ErrorActionPreference = 'Stop'
Set-Location 'C:\TillDock\backend'
Get-Content '.\.env' | ForEach-Object {
    $line = $_
    if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
        $k,$v = $line -split '=',2
        [System.Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), 'Process')
    }
}
if (-not $env:JAVA_HOME) {
    # Fall back to common install locations; override with $env:JAVA_HOME for your machine.
    $candidates = @(
        'C:\Program Files\Java\jdk-21',
        'C:\Program Files\Java\jdk-17',
        'C:\Program Files\Java\jdk-26.0.2',
        'C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot'
    )
    foreach ($c in $candidates) { if (Test-Path "$c\bin\java.exe") { $env:JAVA_HOME = $c; break } }
}
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) { throw "JAVA_HOME not set and no JDK found. Set `$env:JAVA_HOME before running." }
Remove-Item 'C:\TillDock\backend\logs\backend.log' -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path C:\TillDock\backend\logs | Out-Null
$libPath = 'C:\TillDock\backend\target\extracted\BOOT-INF\lib'
$cp = "C:\TillDock\backend\target\extracted\BOOT-INF\classes;$((Get-ChildItem "$libPath\*.jar" | ForEach-Object { $_.FullName }) -join ';')"
$env:DATABASE_URL | Select-Object -First 1 | ForEach-Object { Write-Host "DATABASE_URL host substring: $($_.Substring($_.IndexOf('//')+2, 12))..." }
$args = @('-cp', $cp, 'com.tilldock.auth.AuthApplication')
$proc = Start-Process -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList $args -RedirectStandardOutput 'logs\backend.log' -RedirectStandardError 'logs\backend.err' -WindowStyle Hidden -PassThru
Write-Host "Started backend PID $($proc.Id)"
Start-Sleep -Seconds 8
Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object LocalPort, State, OwningProcess | Format-Table -AutoSize
Write-Host "---log tail---"
Get-Content 'C:\TillDock\backend\logs\backend.log' -Tail 40