$ErrorActionPreference = 'Continue'
$BASE = 'http://localhost:8080'
$A = Get-Content 'C:\TillDock\backend\target\test_a.token'
$B = Get-Content 'C:\TillDock\backend\target\test_b.token'
$busA = '85976e1a-bb59-4726-a38e-bf0f544e03f1'

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

function TestOk {
    param([string]$Label, [object]$Result, [string]$Detail = '')
    if ($Result.ok -and $Result.code -eq 200) { Write-Host "[PASS] $Label  http=$($Result.code)$(if ($Detail) { "  $Detail" })" }
    else { Write-Host "[FAIL] $Label  http=$($Result.code)$(if ($Detail) { "  $Detail" })" }
}

Write-Host "=== R1: GET /api/business (A) ==="
$r = Call-Api -Method GET -Path '/api/business' -Token $A
TestOk -Label 'business read' -Result $r -Detail "name=$($r.data.businessName)"

Write-Host "=== R2: PUT /api/business (A) ==="
$r = Call-Api -Method PUT -Path '/api/business' -Token $A -Body ('{"businessName":"Phase4 Test Co","address":"42 Update Lane","phone":"555-0099"}')
TestOk -Label 'business update' -Result $r -Detail "name=$($r.data.businessName)"

Write-Host "=== R3: GET /api/categories (A) ==="
$r = Call-Api -Method GET -Path '/api/categories' -Token $A
TestOk -Label 'categories list' -Result $r -Detail "count=$($r.data.Count)"

Write-Host "=== R4: POST /api/categories (A) new ==="
$r = Call-Api -Method POST -Path '/api/categories' -Token $A -Body '{"name":"RegTest Cat","description":"regression"}'
$catId = $r.data.id
TestOk -Label 'category create' -Result $r -Detail "id=$catId name=$($r.data.name)"

Write-Host "=== R5: PUT /api/categories/{catId} (A) ==="
$r = Call-Api -Method PUT -Path "/api/categories/$catId" -Token $A -Body '{"name":"RegTest Cat","description":"updated"}'
TestOk -Label 'category update' -Result $r -Detail "desc=$($r.data.description)"

Write-Host "=== R6: GET /api/products (A) ==="
$r = Call-Api -Method GET -Path '/api/products' -Token $A
TestOk -Label 'products list' -Result $r -Detail "count=$($r.data.Count) stockQtyPresent=$($null -ne $r.data[0].stockQuantity) thresholdPresent=$($null -ne $r.data[0].lowStockThreshold)"

Write-Host "=== R7: POST /api/products (A) new with stock ==="
$r = Call-Api -Method POST -Path '/api/products' -Token $A -Body ('{"name":"RegProd","sku":"REGPROD-1","categoryId":"' + $catId + '","purchasePrice":1.00,"sellingPrice":2.00,"stockQuantity":25}')
$prodId = $r.data.id
TestOk -Label 'product create with stock' -Result $r -Detail "id=$prodId stock=$($r.data.stockQuantity) threshold=$($r.data.lowStockThreshold)"

Write-Host "=== R8: GET /api/inventory/{prodId} (newly created product should have INITIAL movement) ==="
$r = Call-Api -Method GET -Path "/api/inventory/$prodId" -Token $A
TestOk -Label 'inventory on new product' -Result $r -Detail "stock=$($r.data.stockQuantity) status=$($r.data.status)"

Write-Host "=== R9: GET movements for new product ==="
$r = Call-Api -Method GET -Path "/api/inventory/$prodId/movements" -Token $A
$types = ($r.data.movementType -join ',')
TestOk -Label 'initial movement exists' -Result $r -Detail "count=$($r.data.Count) types=$types (expect 1: INITIAL)"

Write-Host "=== R10: PUT /api/products/{prodId} (A) update stock quantity ==="
$r = Call-Api -Method PUT -Path "/api/products/$prodId" -Token $A -Body ('{"name":"RegProd","sku":"REGPROD-1","categoryId":"' + $catId + '","purchasePrice":1.00,"sellingPrice":2.00,"stockQuantity":75}')
TestOk -Label 'product update with stock change' -Result $r -Detail "stock=$($r.data.stockQuantity)"

Write-Host "=== R11: GET movements after product update ==="
$r = Call-Api -Method GET -Path "/api/inventory/$prodId/movements" -Token $A
TestOk -Label 'product update recorded adjustment' -Result $r -Detail "count=$($r.data.Count) types=$($r.data.movementType -join ',') (expect 2: ADJUSTMENT, INITIAL)"

Write-Host "=== R12: DELETE /api/categories/{catId} (A) - should fail since product exists ==="
$r = Call-Api -Method DELETE -Path "/api/categories/$catId" -Token $A
if (-not $r.ok -and $r.code -eq 409) { Write-Host "[PASS] delete blocked  http=409  errCode=$((try { ($r.body | ConvertFrom-Json).code } catch {}))" }
else { Write-Host "[FAIL] delete should be blocked http=$($r.code)" }

Write-Host "=== R13: DELETE /api/products/{prodId} (A) ==="
$r = Call-Api -Method DELETE -Path "/api/products/$prodId" -Token $A
if ($r.code -eq 204) { Write-Host "[PASS] product delete http=204" }
else { Write-Host "[FAIL] product delete http=$($r.code)" }

Write-Host "=== R14: DELETE /api/categories/{catId} (A) now succeeds ==="
$r = Call-Api -Method DELETE -Path "/api/categories/$catId" -Token $A
if ($r.code -eq 204) { Write-Host "[PASS] category delete http=204" }
else { Write-Host "[FAIL] category delete http=$($r.code)" }

Write-Host "=== R15: B cannot see A's categories ==="
$catA = '8721e9db-f1cd-4b6a-97b9-5272de6f8174'
$r = Call-Api -Method GET -Path "/api/categories/$catA" -Token $B
if (-not $r.ok -and $r.code -eq 404) { Write-Host "[PASS] B cant see A category http=404" }
else { Write-Host "[FAIL] http=$($r.code)" }

Write-Host ""
Write-Host "=== REGRESSION DONE ==="