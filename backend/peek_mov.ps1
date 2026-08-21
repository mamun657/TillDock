$A = Get-Content 'C:\TillDock\backend\target\test_a.token'
$productId = '49687e8e-1de8-42ce-acc0-185ca8d2858f'
$r = Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/inventory/$productId/movements?page=0&size=3" -Headers @{Authorization="Bearer $A"}
$r | ConvertTo-Json -Depth 5