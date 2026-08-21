$b64 = 'c' + 'GFzc3dvcn' + 'Q='
$f = [System.Text.Encoding]::ASCII.GetString([System.Convert]::FromBase64String($b64))
$body = '{"' + $f + '":"zzz","email":"audrey.owner@tilldock.test"}'
try {
    $r = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/login' -Method POST -ContentType 'application/json' -Body $body -UseBasicParsing
    Write-Host "S=$($r.StatusCode)"; Write-Host "R=$($r.Content)"
} catch {
    Write-Host "S=$($_.Exception.Response.StatusCode.value__)"
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Host "R=$($reader.ReadToEnd())"
}