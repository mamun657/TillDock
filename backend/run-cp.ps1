$ErrorActionPreference = 'Continue'
Set-Location 'C:\TillDock\backend'
if (-not (Test-Path 'target\extracted')) {
    & 'C:\Program Files\Java\jdk-26.0.2\bin\jar.exe' -xf 'target\tilldock-auth-1.0.0.jar' -C 'target\extracted'
    New-Item -ItemType Directory -Path 'target\extracted' -Force | Out-Null
    Push-Location 'target\extracted'
    & 'C:\Program Files\Java\jdk-26.0.2\bin\jar.exe' -xf '..\tilldock-auth-1.0.0.jar'
    Pop-Location
}
Write-Host "Listing BOOT-INF/lib:"
Get-ChildItem 'target\extracted\BOOT-INF\lib' | Measure-Object | Select-Object -ExpandProperty Count
$cp = 'target\extracted\BOOT-INF\classes;'
Get-ChildItem 'target\extracted\BOOT-INF\lib\*.jar' | ForEach-Object { $cp += $_.FullName + ';' }
Write-Host "CP length: $($cp.Length)"
# Get env
Get-Content '.\.env' | ForEach-Object {
    if ($_ -match '^([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}
$env:SERVER_PORT = '8080'
$env:JWT_EXPIRATION_MINUTES = '1440'
$env:BCRYPT_STRENGTH = '12'
Remove-Item 'app.full.out','app.full.err' -ErrorAction SilentlyContinue
$args = @('-cp', $cp, 'com.tilldock.auth.AuthApplication')
$proc = Start-Process -FilePath 'C:\Program Files\Java\jdk-26.0.2\bin\java.exe' `
    -ArgumentList $args `
    -RedirectStandardOutput 'app.full.out' `
    -RedirectStandardError 'app.full.err' `
    -NoNewWindow -PassThru
Write-Host "Started PID $($proc.Id)"
Start-Sleep -Seconds 18
Write-Host "----- app.full.err (last 60) -----"
Get-Content 'app.full.err' -Tail 60 -ErrorAction SilentlyContinue
Write-Host "----- app.full.out (last 30) -----"
Get-Content 'app.full.out' -Tail 30 -ErrorAction SilentlyContinue
Write-Host "----- process status -----"
Get-Process -Id $proc.Id -ErrorAction SilentlyContinue | Select-Object Id,@{N='WS_MB';E={[math]::Round($_.WorkingSet/1MB,1)}},Responding | Format-List
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object State,LocalPort,OwningProcess | Format-Table -AutoSize