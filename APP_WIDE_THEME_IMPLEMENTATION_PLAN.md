# 🎨 App-Wide Theme System - Implementation Plan

## Objective
Apply role-based theming across **ALL activities** in YummyRestaurant app:
- **Customers:** Orange theme (#FF6F00)
- **Staff:** Blue theme (#1976D2)

---

## Solution Architecture

### Option 1: Theme Application Class (Recommended)
Create a centralized `ThemeManager` utility class that:
- Detects user role
- Returns theme colors
- Applies colors to UI elements

### Option 2: Custom Theme Resources
Use Android resource qualifiers:
- Create `colors.xml` for customer theme
- Create `colors-staff.xml` for staff theme
- Apply at runtime

### Option 3: Application-Level Theme
Extend `AppCompatActivity` with base class that applies theme

---

## Recommended Implementation

I'll implement **Option 1 + Option 3 combination**:

### 1. Create `ThemeManager.java` (Utility Class)
```java
public class ThemeManager {
    public static int getPrimaryColor(Context context) {
        if (RoleManager.isStaff()) {
            return Color.parseColor("#1976D2");  // Blue
        }
        return Color.parseColor("#FF6F00");  // Orange
    }
    
    public static void applyThemeToActivity(AppCompatActivity activity) {
        int primaryColor = getPrimaryColor(activity);
        
        // Apply to buttons
        // Apply to toolbars
        // Apply to text
        // Apply to accents
    }
}
```

### 2. Create `BaseActivity.java` (Base Class)
```java
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply theme when any activity loads
        ThemeManager.applyThemeToActivity(this);
    }
}
```

### 3. Update All Activities
```java
// Change from:
public class PaymentActivity extends AppCompatActivity

// Change to:
public class PaymentActivity extends BaseActivity
```

---

## Pages to Update

**All Activities in:** `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/activities/`

- ✅ PaymentActivity (already done)
- ⏳ BrowseMenuActivity
- ⏳ CartActivity  
- ⏳ OrderConfirmationActivity
- ⏳ LoginActivity
- ⏳ RegisterActivity
- ⏳ ProfileActivity
- ⏳ OrderHistoryActivity
- ⏳ KitchenDisplayActivity
- ⏳ And any other activities...

---

## Implementation Steps

1. **Create ThemeManager utility class**
2. **Create BaseActivity base class**
3. **Update all activities to extend BaseActivity**
4. **Remove individual theme code from PaymentActivity**
5. **Define themed UI elements per activity**

---

## Advantages of This Approach

✅ Single place to manage colors
✅ Automatic theme application to all activities
✅ Easy to add new themed elements
✅ Easy to change colors (only update ThemeManager)
✅ Consistent across entire app
✅ Extensible for future themes

---

## Timeline

- ThemeManager: 5 minutes
- BaseActivity: 3 minutes
- Update 10-15 activities: 30-45 minutes
- Testing: 15 minutes

**Total: ~1 hour**

---

## Would you like me to proceed with full implementation?

If yes, I will:
1. Create ThemeManager.java
2. Create BaseActivity.java
3. Update all activities
4. Create comprehensive documentation
5. Verify all pages display correct theme

Shall I continue? (Y/N)
