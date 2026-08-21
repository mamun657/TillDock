$body = @{ email = "[email protected]"; shopName = "Audit Shop" } | ConvertTo-Json
$resp = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/signup" -ContentType "application/json" -Body $body
$resp | ConvertTo-Json -Depth 4 | Out-File -FilePath "C:\TillDock\signup_pre.json" -Encoding utf8
Write-Output ("signup_status=201 email=" + $resp.email + " id=" + $resp.id)