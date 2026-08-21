$ErrorActionPreference = 'Continue'
Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$url = 'http://localhost:8080/api/auth/login'
$path = 'C:\TillDock\r.json'
$body = [System.IO.File]::ReadAllText($path)
$content = New-Object System.Net.Http.StringContent($body, [System.Text.Encoding]::UTF8, 'application/json')
$resp = $client.PostAsync($url, $content).Result
$respBody = $resp.Content.ReadAsStringAsync().Result
Write-Host "STATUS: $($resp.StatusCode)"
Write-Host "BODY: $respBody"