$A = Get-Content 'C:\TillDock\backend\target\test_a.token'
$r = Invoke-RestMethod -Method GET -Uri 'http://localhost:8080/api/inventory' -Headers @{Authorization="Bearer $A"}
$r | Select-Object -First 1 | ConvertTo-Json -Depth 6