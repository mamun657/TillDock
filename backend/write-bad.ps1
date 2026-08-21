$chars = [char[]]@('W','r','o','n','g','P','a','s','s','1','!')
$badpw = -join $chars
$json = '{"email":"audrey.owner@tilldock.test","password":"' + $badpw + '"}'
[System.IO.File]::WriteAllText('C:\TillDock\backend\bad.json', $json, [System.Text.UTF8Encoding]::new($false))
Write-Host "Written length: $((Get-Item 'C:\TillDock\backend\bad.json').Length)"