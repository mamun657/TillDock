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
Remove-Item 'app.full.txt' -ErrorAction SilentlyContinue
$proc = Start-Process -FilePath 'C:\Program Files\Java\jdk-26.0.2\bin\java.exe' `
    -ArgumentList @('--add-opens=java.base/java.lang=ALL-UNNAMED','--add-opens=java.base/java.util=ALL-UNNAMED','--add-opens=java.base/java.lang.invoke=ALL-UNNAMED','--add-opens=java.base/java.net=ALL-UNNAMED','-jar','target\tilldock-auth-1.0.0.jar') `
    -RedirectStandardOutput 'app.full.out' `
    -RedirectStandardError 'app.full.err' `
    -NoNewWindow -PassThru
Write-Host "Started PID $($proc.Id)"
Start-Sleep -Seconds 15
Write-Host "----- app.full.err (last 60) -----"
Get-Content 'app.full.err' -Tail 60 -ErrorAction SilentlyContinue
Write-Host "----- app.full.out (last 30) -----"
Get-Content 'app.full.out' -Tail 30 -ErrorAction SilentlyContinue
Write-Host "----- process status -----"
Get-Process -Id $proc.Id -ErrorAction SilentlyContinue | Select-Object Id,ProcessName,@{N='WS_MB';E={[math]::Round($_.WorkingSet/1MB,1)}},Responding | Format-List
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object State,LocalPort,OwningProcess | Format-Table -AutoSize