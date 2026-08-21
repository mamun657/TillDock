$ErrorActionPreference = 'Stop'
Set-Location 'C:\TillDock\backend'
Get-Content '.\.env' | ForEach-Object {
    $line = $_
    if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
        $k,$v = $line -split '=',2
        [System.Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), 'Process')
    }
}
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-26.0.2'
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