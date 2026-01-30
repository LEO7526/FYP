# App-Wide Theme System Implementation Guide

**Created:** 2024  
**Status:** In Progress  
**Objective:** Apply orange theme (customers) and blue theme (staff) to all 66 activities in the app

---

## 📋 Overview

### What Was Created
1. **ThemeManager.java** - Centralized theme utility with 8+ methods
2. **ThemeBaseActivity.java** - Base class extending AppCompatActivity
3. This documentation guide

### How It Works
```
User Role (RoleManager.isStaff())
        ↓
ThemeManager.getPrimaryColor(context)
        ↓
Theme Applied: 
  - Customers: #FF6F00 (Orange)
  - Staff: #1976D2 (Material Blue)
        ↓
Activity renders with appropriate theme
```

---

## 🔄 Update Process

### Option 1: Extend BaseActivity (RECOMMENDED - Automatic Theming)

**Before:**
```java
public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        // Manual theme setup code here
    }
}
```

**After:**
```java
public class PaymentActivity extends ThemeBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        // Theme is AUTOMATICALLY applied by ThemeBaseActivity!
        // (Removed manual theme setup code)
    }
}
```

**Changes Required:**
1. `extends AppCompatActivity` → `extends ThemeBaseActivity`
2. Remove manual theme application code (if any exists)
3. Remove `import android.app.Activity` if not used elsewhere
4. Add `import com.example.yummyrestaurant.activities.ThemeBaseActivity;`

**Sed Command (Windows PowerShell):**
```powershell
Get-ChildItem -Path "Android\YummyRestaurant\app\src\main\java\com\example\yummyrestaurant\activities\" -Filter "*.java" | ForEach-Object {
    (Get-Content $_.FullName) -replace 'extends AppCompatActivity', 'extends ThemeBaseActivity' | Set-Content $_.FullName
}
```

### Option 2: Keep Existing Base Class + Add Theme Calls

If activities extend a different base class (not AppCompatActivity), add these lines to `onCreate()`:

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_xxx);
    
    // ✅ ADD THESE LINES - Applies theme automatically
    ThemeManager.logThemeApplication(this.getClass().getSimpleName());
    Log.i("ThemeApply", "Theme: " + ThemeManager.getThemeName());
}
```

---

## 🎨 Individual Element Theming

After extending BaseActivity, you can theme specific elements:

### Buttons
```java
Button payButton = findViewById(R.id.pay_button);
applyThemeToButton(payButton);
// or
ThemeManager.applyThemeToButton(payButton, this);
```

### TextViews
```java
TextView amountText = findViewById(R.id.amount_text);
applyThemeToTextView(amountText);
// or
ThemeManager.applyThemeToTextView(amountText, this);
```

### Toolbars
```java
Toolbar toolbar = findViewById(R.id.toolbar);
applyThemeToToolbar(toolbar);
// or
ThemeManager.applyThemeToToolbar(toolbar, this);
```

### Get Theme Color
```java
int color = getThemeColor();
String colorHex = getThemeColorHex();
String themeName = getThemeName();
```

---

## 📁 Files That Need Updating

**Total Activities:** 66 files

### Priority 1 (Core Activities)
- [ ] PaymentActivity.java
- [ ] CartActivity.java
- [ ] MenuActivity.java
- [ ] ProfileActivity.java
- [ ] OrderActivity.java

### Priority 2 (Customer-Facing)
- [ ] BookingActivity.java
- [ ] SearchActivity.java
- [ ] FavoritesActivity.java
- [ ] ReviewActivity.java
- [ ] CheckoutActivity.java

### Priority 3 (Staff Activities)
- [ ] StaffOrderActivity.java
- [ ] KitchenActivity.java
- [ ] ReportActivity.java
- [ ] SettingsActivity.java
- [ ] AnalyticsActivity.java

### Priority 4 (All Others)
- [ ] Remaining 46 activities

---

## 🔧 Implementation Steps

### Step 1: Create BaseActivity ✅ DONE
- Created `ThemeBaseActivity.java` with automatic theme application

### Step 2: Update All Activities
Choose ONE approach:

**Approach A: Using sed/PowerShell (Bulk Update - 5 minutes)**
```powershell
# Update import statements
Get-ChildItem -Path "Android\YummyRestaurant\app\src\main\java\com\example\yummyrestaurant\activities\" -Filter "*.java" | ForEach-Object {
    (Get-Content $_.FullName) `
        -replace 'extends AppCompatActivity', 'extends ThemeBaseActivity' `
        -replace 'import androidx.appcompat.app.AppCompatActivity;', 'import com.example.yummyrestaurant.activities.ThemeBaseActivity;' | `
        Set-Content $_.FullName
}
```

**Approach B: Manual Update by Priority (30 minutes)**
1. Update Priority 1 activities (5 files)
2. Update Priority 2 activities (5 files)
3. Update Priority 3 activities (5 files)
4. Update remaining activities (46 files)

### Step 3: Remove Redundant Code
Search for and remove these patterns in activities:
- `applyThemeColors()` methods
- Manual `button.setBackgroundTintList()` calls
- Manual color application code

### Step 4: Test & Verify
1. Build project: `gradlew build`
2. Run on emulator/device
3. Login as Customer - verify orange theme
4. Login as Staff - verify blue theme
5. Navigate through multiple activities - verify consistency

### Step 5: Compile & Debug
- Check for import errors
- Verify no duplicate method definitions
- Confirm theme applies to all screens

---

## 🎯 Expected Results After Implementation

### Color Scheme

**Customer (isStaff() = false):**
- Primary Color: #FF6F00 (Orange)
- Buttons: Orange background with white text
- Accents: Orange highlights, emphasis colors
- Status: All customer-facing screens themed orange

**Staff (isStaff() = true):**
- Primary Color: #1976D2 (Material Blue)
- Buttons: Blue background with white text
- Accents: Blue highlights, emphasis colors
- Status: All staff-facing screens themed blue

### Verification Checklist
- [ ] All 66 activities compile without errors
- [ ] No duplicate imports after update
- [ ] Customer screens display orange theme
- [ ] Staff screens display blue theme
- [ ] Theme changes correctly on role switch
- [ ] No crashes when switching between roles
- [ ] All UI elements properly themed (buttons, toolbars, text)
- [ ] Color contrast meets accessibility standards

---

## 📊 Progress Tracking

### Phase 1: Planning ✅
- [x] Created ThemeManager.java (8 methods)
- [x] Identified 66 activities
- [x] Created ThemeBaseActivity.java
- [x] Documented implementation strategy

### Phase 2: Update Activities 🔄 IN PROGRESS
- [ ] Create automated update script
- [ ] Update Priority 1 activities
- [ ] Update Priority 2 activities
- [ ] Update Priority 3 activities
- [ ] Update remaining 46 activities

### Phase 3: Testing & Verification ⏳
- [ ] Compile all changes
- [ ] Test on Android emulator
- [ ] Verify customer theme (orange)
- [ ] Verify staff theme (blue)
- [ ] Test on multiple devices
- [ ] Performance check

### Phase 4: Documentation & Cleanup ⏳
- [ ] Remove old applyThemeColors() methods
- [ ] Update activity comments
- [ ] Create final summary
- [ ] Commit changes to version control

---

## 🚀 Quick Start Commands

### Update all activities to extend ThemeBaseActivity:
```powershell
$activityPath = "Android\YummyRestaurant\app\src\main\java\com\example\yummyrestaurant\activities\"
Get-ChildItem -Path $activityPath -Filter "*.java" -Exclude "ThemeBaseActivity.java" | ForEach-Object {
    Write-Host "Updating: $($_.Name)"
    (Get-Content $_.FullName) `
        -replace 'extends AppCompatActivity', 'extends ThemeBaseActivity' `
        -replace 'import androidx.appcompat.app.AppCompatActivity;', 'import com.example.yummyrestaurant.activities.ThemeBaseActivity;' | `
        Set-Content $_.FullName
}
```

### Build and verify:
```powershell
cd "Android\YummyRestaurant"
./gradlew build
```

---

## ⚠️ Important Notes

1. **Import Order:** Make sure `ThemeBaseActivity` import is included before using it
2. **Existing Themes:** Remove any existing `applyThemeColors()` or manual theme code
3. **Testing:** Test theme application on both customer and staff roles
4. **Backwards Compatibility:** Ensure no breaking changes to activity lifecycle
5. **Performance:** Theme application is lightweight - no noticeable performance impact

---

## 📞 Support

### Common Issues & Solutions

**Issue: Import errors after update**
- Solution: Verify correct package path for ThemeBaseActivity

**Issue: Theme not applying**
- Solution: Ensure RoleManager.isStaff() is working correctly

**Issue: Compilation errors**
- Solution: Run `gradlew clean build` to clear build cache

**Issue: Theme applies but colors wrong**
- Solution: Check if RoleManager role is set correctly before theme application

---

## 📝 Summary

This implementation provides:
- ✅ Automatic theme application to all 66 activities
- ✅ Orange theme for customers, blue for staff
- ✅ Centralized color management via ThemeManager
- ✅ Easy extension and customization
- ✅ No external dependencies
- ✅ Standard Android best practices
- ✅ Minimal code changes per activity
- ✅ Reusable theme utilities

**Next Step:** Run the PowerShell script to update all 66 activities, then test!
