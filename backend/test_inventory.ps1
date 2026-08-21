$ErrorActionPreference = 'Continue'
$BASE = 'http://localhost:8080'
$A = Get-Content 'C:\TillDock\backend\target\test_a.token'
$B = Get-Content 'C:\TillDock\backend\target\test_b.token'
$pOld = '8ea57251-7a46-4123-902d-f3edd7cd7f69'
$pNew = '361ac585-7a69-45e2-ba4b-bea3098b9cbe'
$pBulk = '49687e8e-1de8-42ce-acc0-185ca8d2858f'
$bogus = '00000000-0000-0000-0000-000000000000'

function Call-Api {
    param([string]$Method, [string]$Path, [string]$Token, [string]$Body)
    $headers = @{}
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }
    try {
        if ($Body) {
            $headers['Content-Type'] = 'application/json'
            $r = Invoke-RestMethod -Method $Method -Uri ("$BASE$Path") -Headers $headers -Body $Body
            return [PSCustomObject]@{ ok = $true; code = 200; data = $r }
        } else {
            $r = Invoke-RestMethod -Method $Method -Uri ("$BASE$Path") -Headers $headers
            return [PSCustomObject]@{ ok = $true; code = 200; data = $r }
        }
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = [System.IO.StreamReader]::new($stream)
        $body = $reader.ReadToEnd()
        $reader.Close()
        return [PSCustomObject]@{ ok = $false; code = $code; body = $body }
    }
}

function Pass {
    param([string]$Label, [object]$Result, [string]$Detail = '')
    Write-Host "[PASS] $Label  http=$($Result.code)$(if ($Detail) { "  $Detail" })"
}

function Fail {
    param([string]$Label, [object]$Result, [string]$Detail = '')
    Write-Host "[FAIL] $Label  http=$($Result.code)$(if ($Detail) { "  $Detail" })"
}

function TestOk {
    param([string]$Label, [object]$Result, [string]$Detail = '')
    if ($Result.ok -and $Result.code -eq 200) { Pass -Label $Label -Result $Result -Detail $Detail }
    else { Fail -Label $Label -Result $Result -Detail $Detail }
}

function TestErr {
    param([string]$Label, [object]$Result, [int]$ExpectedCode, [string]$ExpectedErrorCode = '')
    $actualCode = $Result.code
    $actualErrCode = ''
    if ($Result.body) { try { $actualErrCode = ($Result.body | ConvertFrom-Json).code } catch {} }
    if (-not $Result.ok -and $actualCode -eq $ExpectedCode) {
        if ($ExpectedErrorCode -and $actualErrCode -ne $ExpectedErrorCode) {
            Write-Host "[FAIL] $Label  http=$actualCode  errCode=$actualErrCode (expected $ExpectedErrorCode)"
        } else {
            Write-Host "[PASS] $Label  http=$actualCode  errCode=$actualErrCode"
        }
    } else {
        Write-Host "[FAIL] $Label  http=$actualCode  errCode=$actualErrCode (expected http=$ExpectedCode code=$ExpectedErrorCode)"
    }
}

Write-Host "=== T1: GET /api/inventory (A) ==="
$r = Call-Api -Method GET -Path '/api/inventory' -Token $A
TestOk -Label 'list inventory' -Result $r -Detail "count=$($r.data.Count) firstStatus=$($r.data[0].status)"

Write-Host "=== T2: GET /api/inventory/{pOld} ==="
$r = Call-Api -Method GET -Path "/api/inventory/$pOld" -Token $A
TestOk -Label 'get inventory item' -Result $r -Detail "name=$($r.data.name) stock=$($r.data.stockQuantity) status=$($r.data.status)"

Write-Host "=== T3: B GET A's product (cross-merchant isolation) ==="
$r = Call-Api -Method GET -Path "/api/inventory/$pOld" -Token $B
TestErr -Label 'cross-merchant isolation' -Result $r -ExpectedCode 404 -ExpectedErrorCode 'product_not_found'

Write-Host "=== T4: GET bogus UUID ==="
$r = Call-Api -Method GET -Path "/api/inventory/$bogus" -Token $A
TestErr -Label 'bogus uuid' -Result $r -ExpectedCode 404 -ExpectedErrorCode 'product_not_found'

Write-Host "=== T5: POST stock-in qty=50 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-in" -Token $A -Body '{"quantity":50,"reason":"supplier shipment"}'
TestOk -Label 'stock-in +50' -Result $r -Detail "stock=$($r.data.stockQuantity) (expect 90) status=$($r.data.status)"

Write-Host "=== T6: POST stock-out qty=30 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-out" -Token $A -Body '{"quantity":30,"reason":"customer order"}'
TestOk -Label 'stock-out -30' -Result $r -Detail "stock=$($r.data.stockQuantity) (expect 60) status=$($r.data.status)"

Write-Host "=== T7: stock-out exceeding stock ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-out" -Token $A -Body '{"quantity":1000,"reason":"too much"}'
TestErr -Label 'insufficient stock' -Result $r -ExpectedCode 409 -ExpectedErrorCode 'insufficient_stock'

Write-Host "=== T8: stock-in quantity=0 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-in" -Token $A -Body '{"quantity":0,"reason":"x"}'
TestErr -Label 'quantity 0' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T9: stock-in quantity=-1 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-in" -Token $A -Body '{"quantity":-1,"reason":"x"}'
TestErr -Label 'quantity -1' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T10: stock-in quantity=1000001 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-in" -Token $A -Body '{"quantity":1000001,"reason":"x"}'
TestErr -Label 'quantity too large' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T11: stock-out qty=1 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-out" -Token $A -Body '{"quantity":1,"reason":"one"}'
TestOk -Label 'stock-out -1' -Result $r -Detail "stock=$($r.data.stockQuantity) (expect 59)"

Write-Host "=== T12: B stock-out A's product ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/stock-out" -Token $B -Body '{"quantity":1,"reason":"hack"}'
TestErr -Label 'B stock-out A' -Result $r -ExpectedCode 404 -ExpectedErrorCode 'product_not_found'

Write-Host "=== T13: adjust newQuantity=100 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/adjust" -Token $A -Body '{"newQuantity":100,"reason":"recount"}'
TestOk -Label 'adjust to 100' -Result $r -Detail "stock=$($r.data.stockQuantity) (expect 100)"

Write-Host "=== T14: adjust newQuantity=0 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/adjust" -Token $A -Body '{"newQuantity":0,"reason":"writeoff"}'
TestOk -Label 'adjust to 0' -Result $r -Detail "stock=$($r.data.stockQuantity) (expect 0) status=$($r.data.status) (expect OUT_OF_STOCK)"

Write-Host "=== T15: adjust newQuantity=-1 ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pNew/adjust" -Token $A -Body '{"newQuantity":-1,"reason":"x"}'
TestErr -Label 'adjust negative' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T16: PATCH threshold=10 ==="
$r = Call-Api -Method PATCH -Path "/api/inventory/$pNew/threshold" -Token $A -Body '{"threshold":10}'
TestOk -Label 'set threshold 10' -Result $r -Detail "threshold=$($r.data.lowStockThreshold) (expect 10) status=$($r.data.status) (expect OUT_OF_STOCK)"

Write-Host "=== T17: PATCH threshold=-1 ==="
$r = Call-Api -Method PATCH -Path "/api/inventory/$pNew/threshold" -Token $A -Body '{"threshold":-1}'
TestErr -Label 'threshold negative' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T18: PATCH threshold=1000001 ==="
$r = Call-Api -Method PATCH -Path "/api/inventory/$pNew/threshold" -Token $A -Body '{"threshold":1000001}'
TestErr -Label 'threshold too large' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T19: B PATCH threshold on A's product ==="
$r = Call-Api -Method PATCH -Path "/api/inventory/$pNew/threshold" -Token $B -Body '{"threshold":5}'
TestErr -Label 'B threshold A' -Result $r -ExpectedCode 404 -ExpectedErrorCode 'product_not_found'

Write-Host "=== T20: GET movements paginated ==="
$r = Call-Api -Method GET -Path "/api/inventory/$pNew/movements?page=0&size=20" -Token $A
TestOk -Label 'list movements' -Result $r -Detail "count=$($r.data.Count) types=$($r.data.movementType -join ',')"

Write-Host "=== T21: GET movements for bogus product ==="
$r = Call-Api -Method GET -Path "/api/inventory/$bogus/movements" -Token $A
TestErr -Label 'movements bogus' -Result $r -ExpectedCode 404 -ExpectedErrorCode 'product_not_found'

Write-Host "=== T22: No auth ==="
$r = Call-Api -Method GET -Path '/api/inventory'
TestErr -Label 'no auth' -Result $r -ExpectedCode 401

Write-Host "=== T23: Bad bearer ==="
$r = Call-Api -Method GET -Path '/api/inventory' -Token 'invalid_token_xyz'
TestErr -Label 'bad bearer' -Result $r -ExpectedCode 401

Write-Host "=== T24: Wrong method (GET on /stock-in) ==="
$r = Call-Api -Method GET -Path "/api/inventory/$pNew/stock-in" -Token $A
TestErr -Label 'wrong method' -Result $r -ExpectedCode 405 -ExpectedErrorCode 'method_not_allowed'

Write-Host "=== T25: bulk item LOW_STOCK ==="
Call-Api -Method POST -Path "/api/inventory/$pBulk/adjust" -Token $A -Body '{"newQuantity":50,"reason":"reset"}' | Out-Null
$r = Call-Api -Method PATCH -Path "/api/inventory/$pBulk/threshold" -Token $A -Body '{"threshold":100}'
TestOk -Label 'bulk LOW_STOCK' -Result $r -Detail "stock=$($r.data.stockQuantity) threshold=$($r.data.lowStockThreshold) status=$($r.data.status) (expect LOW_STOCK)"

Write-Host "=== T26: stock-in no reason ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pBulk/stock-in" -Token $A -Body '{"quantity":25}'
TestOk -Label 'stock-in no reason' -Result $r -Detail "stock=$($r.data.stockQuantity) (expect 75)"

Write-Host "=== T27: stock-out long reason ==="
$longReason = 'x' * 256
$r = Call-Api -Method POST -Path "/api/inventory/$pBulk/stock-out" -Token $A -Body ('{"quantity":1,"reason":"' + $longReason + '"}')
TestErr -Label 'long reason' -Result $r -ExpectedCode 400 -ExpectedErrorCode 'validation_failed'

Write-Host "=== T28: stock-in max qty ==="
$r = Call-Api -Method POST -Path "/api/inventory/$pBulk/stock-in" -Token $A -Body '{"quantity":1000000,"reason":"big"}'
TestOk -Label 'stock-in max qty' -Result $r -Detail "stock=$($r.data.stockQuantity)"

Write-Host "=== T29: set threshold=50 with stock=0 ==="
$r = Call-Api -Method PATCH -Path "/api/inventory/$pNew/threshold" -Token $A -Body '{"threshold":50}'
TestOk -Label 'threshold 50' -Result $r -Detail "status=$($r.data.status) (expect OUT_OF_STOCK)"

Write-Host "=== T30: B inventory list ==="
$r = Call-Api -Method GET -Path '/api/inventory' -Token $B
TestOk -Label 'B inventory list' -Result $r -Detail "count=$($r.data.Count)"

Write-Host ""
Write-Host "=== DONE ==="