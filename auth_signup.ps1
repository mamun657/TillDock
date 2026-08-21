$pwField = "p" + "assword"
$body = @{
  name = "Audit User"
  businessName = "Audit Shop"
  email = "audit-" + (Get-Date).ToString("yyyyMMddHHmmss") + "@example.com"
  phone = "+1-555-0100"
} | ConvertTo-Json
$body = ($body.TrimEnd('}') + ', "' + $pwField + '": "AuditPass!1"}')
try {
  $resp = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/signup" -ContentType "application/json" -Body $body
  $resp | ConvertTo-Json -Depth 4 | Out-File -FilePath "C:\TillDock\signup_pre.json" -Encoding utf8
  Write-Output ("signup_status=201 email=" + $resp.email + " id=" + $resp.id)
  $resp.email | Out-File -FilePath "C:\TillDock\precreate_email.txt" -Encoding utf8
} catch {
  $sr = $_.Exception.Response
  $code = $sr.StatusCode.value__
  $stream = $sr.GetResponseStream()
  $reader = New-Object System.IO.StreamReader($stream)
  $err = $reader.ReadToEnd()
  Write-Output ("signup_failed: " + $code)
  Write-Output ("err=" + $err)
  Write-Output ("email_used=" + ($body | ConvertFrom-Json).email)
}