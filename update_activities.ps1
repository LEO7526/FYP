$activityPath = "C:\xampp\htdocs\newFolder\Android\YummyRestaurant\app\src\main\java\com\example\yummyrestaurant\activities"
$files = @(
    "CartActivity.java",
    "LoginActivity.java",
    "RegisterActivity.java",
    "CustomerHomeActivity.java",
    "BrowseMenuActivity.java",
    "DishDetailActivity.java",
    "CustomizeDishActivity.java",
    "BookingActivity.java",
    "ConfirmBookingActivity.java",
    "OrderHistoryActivity.java",
    "OrderConfirmationActivity.java",
    "OrderTrackingActivity.java",
    "ProfileActivity.java",
    "SettingsActivity.java",
    "EditProfileActivity.java",
    "CouponActivity.java",
    "CouponDetailActivity.java",
    "CouponHistoryActivity.java",
    "MyCouponsActivity.java",
    "ReviewActivity.java",
    "WishlistActivity.java",
    "SupportActivity.java",
    "StoreLocatorActivity.java",
    "PackagesActivity.java",
    "MembershipActivity.java",
    "DashboardActivity.java",
    "BuildSetMenuActivity.java",
    "CheckInAndOutActivity.java",
    "TableOverviewActivity.java",
    "TableOrderDetailActivity.java",
    "TempPaymentActivity.java"
)

$count = 0
foreach ($filename in $files) {
    $filepath = Join-Path $activityPath $filename
    if (Test-Path $filepath) {
        $content = Get-Content $filepath -Raw
        
        # Skip if already updated
        if ($content -like "*extends ThemeBaseActivity*") {
            Write-Host "  $filename (already updated)"
            continue
        }
        
        # Update class declaration
        $updated = $content -replace 'extends AppCompatActivity', 'extends ThemeBaseActivity'
        
        # Update import
        $updated = $updated -replace 'import androidx\.appcompat\.app\.AppCompatActivity;', ''
        
        if ($updated -ne $content) {
            Set-Content $filepath $updated
            $count++
            Write-Host " $filename"
        }
    } else {
        Write-Host " $filename NOT FOUND"
    }
}

Write-Host "`n Total files updated: $count"
