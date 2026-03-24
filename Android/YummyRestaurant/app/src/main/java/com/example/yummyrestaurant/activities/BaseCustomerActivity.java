package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.yummyrestaurant.LoginBottomSheetFragment;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MenuItem;

public abstract class BaseCustomerActivity extends ThemeBaseActivity {

    private static final String TAG = "BottomNav";

    protected BottomNavigationView bottomNavigationView;
    private boolean suppressNavigation = false;
    private boolean login;

    @Override
    protected void onResume() {
        super.onResume();
        login = BrowseMenuActivity.getLogin();

        int selectedIconId = getIntent().getIntExtra("selectedIcon", 0);
        if (selectedIconId != 0 && bottomNavigationView != null) {
            suppressNavigation = true;
            try {
                bottomNavigationView.setSelectedItemId(selectedIconId);
            } catch (Exception ignored) {
                // selectedIconId may not be a nav item (e.g. R.id.addToCartBtn)
            }
            suppressNavigation = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void setupBottomFunctionBar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView == null) {
            // Some layouts override the included root id as bottomFunctionBar.
            View includeRoot = findViewById(R.id.bottomFunctionBar);
            if (includeRoot instanceof BottomNavigationView) {
                bottomNavigationView = (BottomNavigationView) includeRoot;
            }
        }
        if (bottomNavigationView == null) {
            View contentRoot = findViewById(android.R.id.content);
            bottomNavigationView = findBottomNavigationView(contentRoot);
        }
        if (bottomNavigationView == null) {
            Log.w(TAG, "BottomNavigationView not found (bottomNavigationView/bottomFunctionBar)");
            return;
        }

        // Highlight current tab without triggering navigation
        int selectedIconId = getIntent().getIntExtra("selectedIcon", 0);
        if (selectedIconId != 0) {
            suppressNavigation = true;
            try {
                bottomNavigationView.setSelectedItemId(selectedIconId);
            } catch (Exception ignored) {}
            suppressNavigation = false;
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (suppressNavigation) return true;
            int id = item.getItemId();
            int currentSelectedIconId = getIntent().getIntExtra("selectedIcon", 0);
            Log.d(TAG, "onItemSelected tappedId=" + id + ", currentSelectedIconId=" + currentSelectedIconId + ", login=" + login);
            if (id == R.id.orderBellIcon) {
                return navigateProtected(R.id.orderBellIcon, BrowseMenuActivity.class, null, 0, null, null);
            } else if (id == R.id.couponIcon) {
                return navigateProtected(R.id.couponIcon, CouponActivity.class, null, 0, null, null);
            } else if (id == R.id.membershipIcon) {
                return navigateProtected(R.id.membershipIcon, MembershipActivity.class, null, 0, null, null);
            } else if (id == R.id.orderRecordIcon) {
                return navigateProtected(R.id.orderRecordIcon, OrderHistoryActivity.class, null, 0, null, null);
            } else if (id == R.id.profileIcon) {
                return navigateProtected(R.id.profileIcon, ProfileActivity.class, null, 0, null, null);
            }
            Log.w(TAG, "Unknown bottom nav item id=" + id + ", keeping current tab");
            return false;
        });

        // Suppress reselection (no re-launch if already on this screen)
        bottomNavigationView.setOnItemReselectedListener(item -> {});
    }

    /**
     * Checks login state; BrowseMenuActivity and CouponActivity are always allowed.
     * Other activities require login. Optionally passes pending cart extras.
     */
    protected boolean navigateProtected(int iconId,
                                     Class<? extends ThemeBaseActivity> target,
                                     MenuItem pendingItem,
                                     int pendingQuantity,
                                     String pendingSpice,
                                     String pendingNotes) {
        return navigateProtectedInternal(iconId, target, pendingItem, pendingQuantity, pendingSpice, pendingNotes);
    }

    private boolean navigateProtectedInternal(int iconId,
                                     Class<? extends ThemeBaseActivity> target,
                                     MenuItem pendingItem,
                                     int pendingQuantity,
                                     String pendingSpice,
                                     String pendingNotes) {
        if (target == BrowseMenuActivity.class || target == CouponActivity.class) {
            Log.d(TAG, "Navigate allowed without login. target=" + target.getSimpleName() + ", iconId=" + iconId);
            launchScreen(iconId, target, null, 0, null, null);
            return true;
        }

        if (login) {
            Log.d(TAG, "Navigate allowed (logged in). target=" + target.getSimpleName() + ", iconId=" + iconId);
            launchScreen(iconId, target, pendingItem, pendingQuantity, pendingSpice, pendingNotes);
            return true;
        } else {
            Log.i(TAG, "Navigate blocked (not logged in). target=" + target.getSimpleName() + ", iconId=" + iconId + ". Showing login sheet.");
            showInlineLogin(
                    () -> {
                        Log.i(TAG, "Login success from sheet. Continue navigation to " + target.getSimpleName());
                        launchScreen(iconId, target, pendingItem, pendingQuantity, pendingSpice, pendingNotes);
                    },
                    pendingItem, pendingQuantity, pendingSpice, pendingNotes
            );
            return false;
        }
    }

    /**
     * Actually starts the target Activity and passes along the selected icon ID.
     */
    private void launchScreen(int iconId,
                              Class<? extends ThemeBaseActivity> cls,
                              MenuItem pendingItem,
                              int pendingQuantity,
                              String pendingSpice,
                              String pendingNotes) {
        int currentSelectedIconId = getIntent().getIntExtra("selectedIcon", 0);
        if (currentSelectedIconId == iconId) {
            Log.d(TAG, "Skip relaunch for same tab. iconId=" + iconId + ", target=" + cls.getSimpleName());
            return;
        }

        Log.d(TAG, "Launching screen. fromIcon=" + currentSelectedIconId + ", toIcon=" + iconId + ", target=" + cls.getSimpleName());
        Intent intent = new Intent(this, cls);
        intent.putExtra("selectedIcon", iconId);

        if (cls == CustomizeDishActivity.class) {
            MenuItem dish = (MenuItem) getIntent().getSerializableExtra("menuItem");
            int qty = getIntent().getIntExtra("quantity", 1);
            if (dish != null) {
                intent.putExtra(CustomizeDishActivity.EXTRA_MENU_ITEM, dish);
                intent.putExtra(CustomizeDishActivity.EXTRA_QUANTITY, qty);
            }
        }

        if (pendingItem != null && cls != CustomizeDishActivity.class) {
            intent.putExtra("pendingMenuItem", pendingItem);
            intent.putExtra("pendingQuantity", pendingQuantity);
            intent.putExtra("pendingSpice", pendingSpice);
            intent.putExtra("pendingNotes", pendingNotes);
        }

        startActivity(intent);
    }

    /**
     * Shows the inline login bottom sheet, passing pending cart extras.
     */
    protected void showInlineLogin(Runnable onSuccess,
                                 MenuItem pendingItem,
                                 int pendingQuantity,
                                 String pendingSpice,
                                 String pendingNotes) {

        Bundle args = new Bundle();
        if (pendingItem != null) {
            args.putSerializable("pendingMenuItem", pendingItem);
            args.putInt("pendingQuantity", pendingQuantity);
            args.putString("pendingSpice", pendingSpice);
            args.putString("pendingNotes", pendingNotes);
        }

        LoginBottomSheetFragment sheet = new LoginBottomSheetFragment();
        sheet.setArguments(args);

        sheet.setLoginListener(success -> {
            if (success) {
                login = true;
                if (onSuccess != null) onSuccess.run();
            }
        });

        sheet.show(getSupportFragmentManager(), "login_sheet");
    }

    private BottomNavigationView findBottomNavigationView(View view) {
        if (view == null) return null;
        if (view instanceof BottomNavigationView) {
            return (BottomNavigationView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                BottomNavigationView found = findBottomNavigationView(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
