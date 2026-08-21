$ErrorActionPreference = 'Continue'
Set-Location 'C:\TillDock\backend'
Get-Content '.\.env' | ForEach-Object {
    if ($_ -match '^([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}
$env:SERVER_PORT = '8080'
$env:JWT_EXPIRATION_MINUTES = '1440'
$env:BCRYPT_STRENGTH = '12'
if (-not (Test-Path 'target\extracted\BOOT-INF\classes')) {
    New-Item -ItemType Directory -Path 'target\extracted' -Force | Out-Null
    Push-Location 'target\extracted'
    & 'C:\Program Files\Java\jdk-26.0.2\bin\jar.exe' -xf '..\tilldock-auth-1.0.0.jar'
    Pop-Location
}
$cp = 'target\extracted\BOOT-INF\classes;'
Get-ChildItem 'target\extracted\BOOT-INF\lib\*.jar' | ForEach-Object { $cp += $_.FullName + ';' }
$proc = Start-Process -FilePath 'C:\Program Files\Java\jdk-26.0.2\bin\java.exe' `
    -ArgumentList @('-cp', $cp, 'com.tilldock.auth.AuthApplication') `
    -RedirectStandardOutput 'app.full.out' `
    -RedirectStandardError 'app.full.err' `
    -WindowStyle Hidden -PassThru
Write-Host "Started PID $($proc.Id)"
$proc | Format-List Id, ProcessName, StartTime
Start-Sleep -Seconds 15
Get-Process -Id $proc.Id -ErrorAction SilentlyContinue | Select-Object Id, @{N='WS_MB';E={[math]::Round($_.WorkingSet/1MB,1)}}, Responding | Format-List
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object State,LocalPort,OwningProcess | Format-Table -AutoSize