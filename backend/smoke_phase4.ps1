$ErrorActionPreference = "Stop"
$api = "http://localhost:8080/api"

function Call-Api {
    param($Method, $Path, $Token = $null, $Body = $null)
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $args = @{ Uri = "$api$Path"; Method = $Method; Headers = $headers; ContentType = "application/json" }
    if ($Body) { $args.Body = $Body }
    try {
        $res = Invoke-RestMethod @args -ErrorAction Stop
        $items = @($res)
        return [pscustomobject]@{ ok = $true; items = $items; data = $items[0] }
    } catch {
        $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        $msg = $_.Exception.Message
        if ($_.ErrorDetails.Message) { $msg = $_.ErrorDetails.Message }
        return [pscustomobject]@{ ok = $false; code = $code; body = $msg }
    }
}

function Must([bool]$cond, [string]$label) {
    if ($cond) { Write-Host ("  PASS " + $label) -ForegroundColor Green }
    else { Write-Host ("  FAIL " + $label) -ForegroundColor Red; $script:Fails++ }
}

$script:Fails = 0

Write-Host "=== Login merchant A ==="
$r = Call-Api POST "/auth/login" $null '{"email":"emulator20260818@example.com","password":"Test1234"}'
Must $r.ok "login ok"
$tokenA = $r.data.token

Write-Host ""
Write-Host "=== 1) LIST inventory ==="
$r = Call-Api GET "/inventory" $tokenA
Must $r.ok "list returned 200"
Write-Host ("  count=" + $r.items.Count)
$first = $r.items[0]
$prodId = $first.productId
Write-Host ("  productId=" + $prodId + " stock=" + $first.stockQuantity + " status=" + $first.status)

Write-Host ""
Write-Host "=== 2) GET by productId ==="
$r = Call-Api GET ("/inventory/" + $prodId) $tokenA
Must $r.ok "get returned 200"
Write-Host ("  name=" + $r.data.name + " stock=" + $r.data.stockQuantity + " threshold=" + $r.data.lowStockThreshold)

Write-Host ""
Write-Host "=== 3) STOCK-IN +5 ==="
$r = Call-Api POST ("/inventory/" + $prodId + "/stock-in") $tokenA '{"quantity":5,"reason":"restock from supplier"}'
Must $r.ok "stock-in ok"
Write-Host ("  new stock=" + $r.data.stockQuantity)

Write-Host ""
Write-Host "=== 4) ADJUST to 50 ==="
$r = Call-Api POST ("/inventory/" + $prodId + "/adjust") $tokenA '{"newQuantity":50,"reason":"cycle count correction"}'
Must $r.ok "adjust ok"
Write-Host ("  new stock=" + $r.data.stockQuantity)

Write-Host ""
Write-Host "=== 5) STOCK-OUT 3 ==="
$r = Call-Api POST ("/inventory/" + $prodId + "/stock-out") $tokenA '{"quantity":3,"reason":"manual sale"}'
Must $r.ok "stock-out ok"
Write-Host ("  new stock=" + $r.data.stockQuantity)

Write-Host ""
Write-Host "=== 6) STOCK-OUT 9999 (must FAIL: insufficient stock) ==="
$r = Call-Api POST ("/inventory/" + $prodId + "/stock-out") $tokenA '{"quantity":9999,"reason":"test atomic guard"}'
Must (-not $r.ok) "stock-out 9999 rejected"
Must ($r.code -ge 400 -and $r.code -lt 500) ("stock-out 9999 returned 4xx (got " + $r.code + ")")
Write-Host ("  rejected with code=" + $r.code)

Write-Host ""
Write-Host "=== 7) SET threshold to 10 ==="
$r = Call-Api PATCH ("/inventory/" + $prodId + "/threshold") $tokenA '{"threshold":10}'
Must $r.ok "set-threshold ok"
Write-Host ("  new threshold=" + $r.data.lowStockThreshold + " status=" + $r.data.status)

Write-Host ""
Write-Host "=== 8) MOVEMENTS history ==="
$r = Call-Api GET ("/inventory/" + $prodId + "/movements?page=0&size=20") $tokenA
Must $r.ok "movements ok"
Write-Host ("  movements count=" + $r.items.Count)
foreach ($m in $r.items) {
    Write-Host ("    [" + $m.movementType + "] delta=" + $m.delta + " " + $m.previousQuantity + " -> " + $m.newQuantity + " reason='" + $m.reason + "'")
}

Write-Host ""
Write-Host "=== 9) Cross-merchant isolation ==="
$r = Call-Api POST "/auth/login" $null '{"email":"security-test@example.com","password":"Test1234!"}'
if ($r.ok) {
    $tokenB = $r.data.token
    $r = Call-Api GET ("/inventory/" + $prodId) $tokenB
    Must (-not $r.ok) "merchant B denied"
    Must ($r.code -eq 404) ("merchant B gets 404 (got " + $r.code + ")")
} else {
    Write-Host "  (skipping - login B failed)" -ForegroundColor Yellow
}

Write-Host ""
if ($script:Fails -eq 0) {
    Write-Host "=== Phase 4 smoke test: ALL CHECKS PASSED ===" -ForegroundColor Green
    exit 0
} else {
    Write-Host ("=== Phase 4 smoke test: " + $script:Fails + " FAILURE(S) ===") -ForegroundColor Red
    exit 1
}
