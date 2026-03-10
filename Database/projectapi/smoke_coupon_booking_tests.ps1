param(
    [int]$Cid = 1,
    [int]$DaysAhead = 3,
    [int]$GuestCount = 2,
    [string]$Times = '12:00,18:30,20:00',
    [switch]$SkipIntegration
)

$ErrorActionPreference = 'Stop'

$base = 'http://127.0.0.1/newFolder/Database/projectapi'
$results = New-Object System.Collections.Generic.List[Object]

function Invoke-CurlApi {
    param(
        [string]$Name,
        [string[]]$CurlArgs,
        [int[]]$ExpectedStatus,
        [scriptblock]$Assert
    )

    $allArgs = @('-s', '-w', "`n__STATUS__:%{http_code}") + $CurlArgs
    $rawOutput = & curl.exe @allArgs

    $status = -1
    $body = [string]$rawOutput

    if ($body -match "__STATUS__:(\d{3})$") {
        $status = [int]$Matches[1]
        $body = $body.Substring(0, $body.Length - ("__STATUS__:" + $Matches[1]).Length).TrimEnd("`r", "`n")
    }

    $json = $null
    try {
        $json = $body | ConvertFrom-Json
    } catch {
        $json = $null
    }

    $statusOk = $ExpectedStatus -contains $status
    $assertOk = $false
    $message = ''

    try {
        if ($null -ne $Assert) {
            $assertOk = & $Assert $json $body $status
        } else {
            $assertOk = $true
        }
    } catch {
        $assertOk = $false
        $message = 'Assert exception: ' + $_.Exception.Message
    }

    $pass = $statusOk -and $assertOk

    if ([string]::IsNullOrWhiteSpace($message)) {
        if (-not $statusOk) {
            $message = "Status $status not in [$($ExpectedStatus -join ',')]"
        } elseif (-not $assertOk) {
            $message = 'Assertion failed'
        } else {
            $message = 'OK'
        }
    }

    $results.Add([pscustomobject]@{
        Test = $Name
        Status = $status
        Pass = $pass
        Message = $message
    }) | Out-Null

    if (-not $pass) {
        Write-Output "--- FAIL RAW ($Name) ---"
        if ($body.Length -gt 500) {
            Write-Output $body.Substring(0, 500)
        } else {
            Write-Output $body
        }
    }

    return [pscustomobject]@{ Status = $status; Json = $json; Body = $body; Pass = $pass }
}

# Seed coupon id for detail test
$couponList = Invoke-CurlApi -Name 'Coupon List' -CurlArgs @("$base/getCoupons.php?cid=$Cid&lang=en") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $true -and $null -ne $j.coupons)
}

$couponId = 1
if ($couponList.Json -and $couponList.Json.coupons -and $couponList.Json.coupons.Count -gt 0) {
    $couponId = [int]$couponList.Json.coupons[0].coupon_id
}

$futureDate = (Get-Date).AddDays(2).ToString('yyyy-MM-dd')
$time = '18:30'

# Coupon tests
Invoke-CurlApi -Name 'Coupon Points (coupon_point API)' -CurlArgs @("$base/getCouponPoints.php?cid=$Cid") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $true -and $j.PSObject.Properties.Name -contains 'points')
} | Out-Null

Invoke-CurlApi -Name 'Coupon Points (customer API)' -CurlArgs @("$base/get_customer_coupon_points.php?cid=$Cid") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.PSObject.Properties.Name -contains 'coupon_points')
} | Out-Null

Invoke-CurlApi -Name 'Coupon History' -CurlArgs @("$base/getCouponHistory.php?cid=$Cid&lang=en") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $true -and $j.PSObject.Properties.Name -contains 'history')
} | Out-Null

Invoke-CurlApi -Name 'My Coupons' -CurlArgs @("$base/getMyCoupons.php?cid=$Cid&lang=en") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $true -and $j.PSObject.Properties.Name -contains 'coupons')
} | Out-Null

Invoke-CurlApi -Name 'Coupon Detail Valid' -CurlArgs @("$base/getCouponDetail.php?coupon_id=$couponId&lang=en") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.PSObject.Properties.Name -contains 'success')
} | Out-Null

Invoke-CurlApi -Name 'Coupon Detail Invalid ID' -CurlArgs @("$base/getCouponDetail.php?coupon_id=0&lang=en") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $false)
} | Out-Null

Invoke-CurlApi -Name 'Redeem Coupon Invalid ID' -CurlArgs @('-X', 'POST', '-d', ("cid=$Cid&coupon_id=999999&quantity=1"), "$base/redeemCoupon.php") -ExpectedStatus @(500) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $false)
} | Out-Null

Invoke-CurlApi -Name 'Use Coupon Missing Quantities' -CurlArgs @('-X', 'POST', '-d', ("cid=$Cid&order_total=1000&order_type=takeaway"), "$base/useCoupon.php") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $false)
} | Out-Null

# Booking tests
Invoke-CurlApi -Name 'Get My Bookings' -CurlArgs @("$base/get_my_bookings.php?cid=$Cid") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $true -and $j.PSObject.Properties.Name -contains 'data')
} | Out-Null

Invoke-CurlApi -Name 'Available Tables Missing Params' -CurlArgs @("$base/get_available_tables.php") -ExpectedStatus @(400) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $false)
} | Out-Null

Invoke-CurlApi -Name 'Available Tables Valid' -CurlArgs @("$base/get_available_tables.php?date=$futureDate&time=$time&pnum=$GuestCount") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($j -is [System.Array])
} | Out-Null

Invoke-CurlApi -Name 'Available Tables Layout Valid' -CurlArgs @("$base/get_available_tables_layout.php?date=$futureDate&time=$time&pnum=$GuestCount") -ExpectedStatus @(200) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $true -and $j.PSObject.Properties.Name -contains 'tables')
} | Out-Null

$invalidCreateBookingPayload = '{"cid":' + $Cid + ',"bkcname":"","bktel":"","tid":0,"bdate":"","btime":"","pnum":0}'
Invoke-CurlApi -Name 'Create Booking Missing Fields' -CurlArgs @('-X', 'POST', '-H', 'Content-Type: application/json', '--data-raw', $invalidCreateBookingPayload, "$base/create_booking.php") -ExpectedStatus @(400) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $false)
} | Out-Null

$invalidCancelPayload = '{"bid":0,"cid":' + $Cid + '}'
Invoke-CurlApi -Name 'Cancel Booking Invalid IDs' -CurlArgs @('-X', 'POST', '-H', 'Content-Type: application/json', '--data-raw', $invalidCancelPayload, "$base/cancel_booking.php") -ExpectedStatus @(400) -Assert {
    param($j, $b, $s)
    return ($null -ne $j -and $j.success -eq $false)
} | Out-Null

$integrationScript = Join-Path $PSScriptRoot 'integration_coupon_booking_rollback_test.php'
if ($SkipIntegration) {
    $results.Add([pscustomobject]@{
        Test = 'Integration Positive + Rollback'
        Status = 0
        Pass = $true
        Message = 'Skipped by -SkipIntegration'
    }) | Out-Null
} elseif (Test-Path $integrationScript) {
    $integrationOutput = & php $integrationScript "--cid=$Cid" "--days-ahead=$DaysAhead" "--pnum=$GuestCount" "--times=$Times" 2>&1
    $integrationExit = $LASTEXITCODE
    $integrationPass = ($integrationExit -eq 0)
    $integrationStatus = if ($integrationPass) { 200 } else { 500 }
    $integrationMessage = if ($integrationPass) { 'OK' } else { "ExitCode=$integrationExit" }

    $results.Add([pscustomobject]@{
        Test = 'Integration Positive + Rollback'
        Status = $integrationStatus
        Pass = $integrationPass
        Message = $integrationMessage
    }) | Out-Null

    if (-not $integrationPass) {
        Write-Output '--- FAIL RAW (Integration Positive + Rollback) ---'
        $integrationText = ($integrationOutput | Out-String).Trim()
        if ($integrationText.Length -gt 1200) {
            Write-Output $integrationText.Substring(0, 1200)
        } else {
            Write-Output $integrationText
        }
    }
} else {
    $results.Add([pscustomobject]@{
        Test = 'Integration Positive + Rollback'
        Status = 500
        Pass = $false
        Message = 'integration_coupon_booking_rollback_test.php not found'
    }) | Out-Null
}

$passCount = ($results | Where-Object { $_.Pass }).Count
$totalCount = $results.Count
$failCount = $totalCount - $passCount

Write-Output ''
Write-Output '===== Coupon + Booking Smoke Test Summary ====='
$results | Format-Table -AutoSize | Out-String | Write-Output
Write-Output ("Total: {0}, Passed: {1}, Failed: {2}" -f $totalCount, $passCount, $failCount)

if ($failCount -gt 0) {
    exit 1
}

exit 0
