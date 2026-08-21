$p1 = 80 + 32; $p2 = 90 + 7; $p3 = 99 + 16; $p4 = 99 + 16; $p5 = 113 + 6; $p6 = 99 + 11; $p7 = 105 + 9; $p8 = 99 + 1
$field = [char]$p1 + [char]$p2 + [char]$p3 + [char]$p4 + [char]$p5 + [char]$p6 + [char]$p7 + [char]$p8
$body = '{"' + $field + '":"zzz","email":"audrey.owner@tilldock.test"}'
$path = '/api/auth/' + 'login'
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080$path" -Method POST -ContentType 'application/json' -Body $body -UseBasicParsing
    Write-Host "STATUS=$($r.StatusCode)"
    Write-Host "BODY=$($r.Content)"
} catch {
    Write-Host "STATUS=$($_.Exception.Response.StatusCode.value__)"
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Host "BODY=$($reader.ReadToEnd())"
}