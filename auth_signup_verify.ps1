$json = Get-Content 'C:\TillDock\signup_pre.json' -Raw
$obj = $json | ConvertFrom-Json
$token = $obj.token
$me = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/auth/me" -Headers @{ Authorization = "Bearer $token" }
Write-Output ("me_id=" + $me.id)
Write-Output ("me_email=" + $me.email)
Write-Output ("me_status=" + $me.status)
Write-Output ("me_lastLoginAt=" + $me.lastLoginAt)
Write-Output ("me_createdAt=" + $me.createdAt)